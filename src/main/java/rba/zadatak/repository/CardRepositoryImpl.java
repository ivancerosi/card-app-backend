package rba.zadatak.repository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rba.zadatak.entity.CardApplicant;

import java.io.File;
import java.util.Optional;

@Component
public class CardRepositoryImpl implements CardRepository {
    FileRepositoryFragment<CardApplicant, String> fileRepositoryFragment;
    JPARepositoryFragment jpaFragment;

    public CardRepositoryImpl(JPARepositoryFragment jpaFragment, FileRepositoryFragment<CardApplicant,String> fileRepositoryFragment) {
        this.jpaFragment = jpaFragment;
        this.fileRepositoryFragment=fileRepositoryFragment;
    }

    @Override
    public CardApplicant save(CardApplicant entity) {
        return jpaFragment.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CardApplicant saveForceCommit(CardApplicant entity) {
        return jpaFragment.save(entity);
    }


    @Override
    public Optional<CardApplicant> findById(String s) {
        return jpaFragment.findById(s);
    }

    @Override
    public Optional<CardApplicant> findByIdPessimisticWrite(String pin) {
        return jpaFragment.findByIdPessimisticWrite(pin);
    }

    @Override
    public Optional<CardApplicant> findByIdPessimisticRead(String pin) {
        return jpaFragment.findByIdPessimisticRead(pin);
    }

    @Override
    public Boolean existsById(String s) {
        return jpaFragment.existsById(s);
    }


    @Override
    public void deleteById(String s) {
        jpaFragment.deleteById(s);
    }


    @Override
    public CardApplicant readFile(File file) {
        return fileRepositoryFragment.readFile(file);
    }

    @Override
    public File writeFile(CardApplicant entity) {
        return fileRepositoryFragment.writeFile(entity);
    }

    @Override
    public CardApplicant updateFile(CardApplicant entity, File file) {
        return fileRepositoryFragment.updateFile(entity, file);
    }

    @Override
    public void deleteFile(File file) {
        fileRepositoryFragment.deleteFile(file);
    }
}
