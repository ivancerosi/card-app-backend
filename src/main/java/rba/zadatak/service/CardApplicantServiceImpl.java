package rba.zadatak.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rba.zadatak.entity.CardApplicant;
import rba.zadatak.enums.CardStatus;
import rba.zadatak.exceptions.CardApplicantFileError;
import rba.zadatak.exceptions.UserAlreadyExistsException;
import rba.zadatak.exceptions.UserDoesNotExistException;
import rba.zadatak.repository.CardRepository;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
public class CardApplicantServiceImpl implements CardApplicantService {
    private final CardRepository cardRepository;

    private static final Logger logger = LoggerFactory.getLogger("rba.zadatak");



    private final ConcurrentMap<String, WeakReference<Object>> locks = new ConcurrentHashMap<>();

    private synchronized Object getEntityLock(String pin) {

        // WeakReference sluzi kako bi osigurali da Gargabe Collector ukloni nekoristene lock objekte
        locks.putIfAbsent(pin, new WeakReference<>(new Object()));
        return locks.get(pin).get();
    }

    public CardApplicantServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }


    // vraca korisnika i istovremeno stvara tekstualnu datoteku
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<CardApplicant> findApplicant(String pin) {

        // nitko ne smije citati ni pisati u ovaj CardApplicant record dok transakcija traje
        Optional<CardApplicant> applicant = cardRepository.findByIdPessimisticWrite(pin);

        if (applicant.isPresent()) {
                switch (applicant.get().getStatus()) {
                    case ACCEPTED:
                        // vec bi trebala postojati kreirana datoteka
                        if (applicant.get().getFilename()==null) {
                            // CardApplicant je upisan kao ACCEPTED, ali nema datoteku upisanu u svoj record
                            logger.error("CardApplicant (pin: {}) is set as {} but doesn't have text file name written" +
                                    " in their record", applicant.get().getPin(), CardStatus.ACCEPTED);

                            throw new CardApplicantFileError(String.format("CardApplicant is set as %s but doesn't have " +
                                    "text file name stored in their record", CardStatus.ACCEPTED));
                        }
                        if (!new File(applicant.get().getFilename()).exists()) {
                            // ako je datoteka upisana, ali ne postoji ...
                            logger.error("Applicant (pin: {}) is marked as {} but corresponding text file is missing",
                                    applicant.get().getPin(),
                                    CardStatus.ACCEPTED);

                            throw new CardApplicantFileError(String.format("CardApplicant is set as %s but corresponding " +
                                    "text file is missing", CardStatus.ACCEPTED));
                        }
                        break;
                    case PENDING:
                        applicant.get().setStatus(CardStatus.ACCEPTED);
                        String filename = cardRepository.writeFile(applicant.get()).getName();
                        try {
                            applicant.get().setFilename(filename);
                            cardRepository.save(applicant.get());
                        }
                        catch (RuntimeException e) {
                            // file rollback ako upis u bazu ne uspije
                            cardRepository.deleteFile(new File(filename));
                        }

                        break;

                    default:
                        // ovo se ne bi smjelo dogoditi
                        logger.error("Retrieved applicant with status ${} For applicant pin {}",
                                applicant.get().getStatus(),
                                applicant.get().getPin());
                        throw new CardApplicantFileError(String.format("Retrieved CardApplicant with illegal CardStatus(%s)",
                                applicant.get().getStatus()));
                }
            }
        return applicant;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteApplicant(String pin) {

        Optional<CardApplicant> applicant = cardRepository.findByIdPessimisticWrite(pin);

        if (!applicant.isPresent()) {
            // delete je idempotentna operacija tako da ne treba podignuti iznimku ako korisnik vec ne postoji
            logger.info("Delete attempted on CardApplicant(pin: {}) who was already deleted from the database", pin);
            return;
        }

        CardStatus status = applicant.get().getStatus();
        if (status == CardStatus.DELETED) {
            logger.warn("deleteApplicant invoked on applicant(pin: {}) who has status {} but is still in the database",
                    pin,
                    CardStatus.DELETED
                    );
        }

        cardRepository.deleteById(pin);

        // ako je status ACCEPTED onda korisnik ima aktivnu datoteku koju je potrebno deaktivirati
        if (applicant.get().getStatus()==CardStatus.ACCEPTED) {
            try {
                applicant.get().setStatus(CardStatus.DELETED);
                cardRepository.updateFile(applicant.get(), new File(applicant.get().getFilename()));
            } catch (Exception e) {
                logger.error("Unable to modify CardApplicant's text file during deletion process", e);
                // Exception handler u Controlleru ce uloviti ovu gresku, a transakcija u RDBMS-u ce se rollbackat
                throw new CardApplicantFileError("Unable to modify CardApplicant's text file during deletion process", e);
            }
        }
    }

    @Override
    public CardApplicant storeApplicant(CardApplicant applicant) {

        synchronized (getEntityLock(applicant.getPin())) {
            if (cardRepository.findById(applicant.getPin()).isPresent()) {
                // korisnik vec postoji, podigni iznimku
                throw new UserAlreadyExistsException(String.format("" +
                        "User with PIN: %s already exists", applicant.getPin()
                ));
            }
            // prisili commit tako da sljedeca invokacija ove metode dohvati entity iz relacijske baze
            return cardRepository.saveForceCommit(applicant);
        }
    }

    // nije navedeno u zadatku, ali CRUD aplikacije obicno nude i Update endpoint
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CardApplicant editApplicant(CardApplicant applicant) {
        Optional<CardApplicant> found = cardRepository.findByIdPessimisticWrite(applicant.getPin());

        if (!found.isPresent()) {
            // korisnik nije unesen, vrati gresku
            logger.warn("Tried to modify an un-existing user (requested pin: {})",applicant.getPin());
            throw new UserDoesNotExistException(String.format("User with PIN: %s does not exist",applicant.getPin()));
        }

        // ako je novi objekt isti tada nije potrebno prolaziti kroz proces
        if (applicant.equals(found.get())) return found.get();

        switch (found.get().getStatus()) {
            case PENDING:
                // samo promijeni entity u relacijskoj bazi
                found.get().setStatus(CardStatus.PENDING);
                applicant=cardRepository.save(found.get());
                return applicant;
            case ACCEPTED:
                // vec bi trebala postojati kreirana datoteka koju je potrebno oznaciti neaktivnom
                if (found.get().getFilename()==null) {
                    // CardApplicant je upisan kao ACCEPTED, ali nema datoteku upisanu u svoj record
                    logger.error("CardApplicant (pin: {}) is set as {} but doesn't have text file name written" +
                            " in their record", found.get().getPin(), CardStatus.ACCEPTED);

                    throw new CardApplicantFileError(String.format("CardApplicant is set as %s but doesn't have " +
                            "text file name stored in their record", CardStatus.ACCEPTED));
                }
                if (!new File(found.get().getFilename()).exists()) {
                    // ako je datoteka upisana, ali ne postoji ...
                    logger.error("Applicant (pin: {}) is marked as {} but corresponding text file is missing",
                            found.get().getPin(),
                            CardStatus.ACCEPTED);

                    throw new CardApplicantFileError(String.format("CardApplicant is set as %s but corresponding " +
                            "text file is missing", CardStatus.ACCEPTED));
                }
                // ako se prethodne provjere prosle... promijeni entity record u bazi i oznaci datoteku kao neaktivnu
                CardStatus prevStatus = found.get().getStatus();
                found.get().setStatus(CardStatus.OUTDATED);
                cardRepository.updateFile(found.get(), new File(found.get().getFilename()));

                try {
                    applicant = cardRepository.save(applicant);
                } catch (RuntimeException e) {
                    // rollback file
                    found.get().setStatus(prevStatus);
                    cardRepository.updateFile(found.get(), new File(found.get().getFilename()));
                }
                return cardRepository.save(applicant);
            default:
                // ovo se ne bi smjelo dogoditi
                logger.error("Tried to modfiy applicant with status ${} For applicant pin {}",
                        applicant.getStatus(),
                        applicant.getPin());
                throw new CardApplicantFileError(String.format("Tried to modify CardApplicant containing illegal CardStatus(%s)",
                        applicant.getStatus()));

        }
    }

}
