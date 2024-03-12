package rba.zadatak.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import rba.zadatak.entity.CardApplicant;

import java.util.Optional;

/*
 ** Spring JPA automatski generira implementaciju ovog sucelja
 */
public interface JPARepositoryFragment extends CrudRepository<CardApplicant, String> {

    @Query("select c from CardApplicant c where c.pin = ?1")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CardApplicant> findByIdPessimisticWrite(String pin);

    @Query("select c from CardApplicant c where c.pin = ?1")
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<CardApplicant> findByIdPessimisticRead(String pin);

    CardApplicant save(CardApplicant entity);

    void deleteAll();
}
