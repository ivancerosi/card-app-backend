package rba.zadatak.repository;


import rba.zadatak.entity.CardApplicant;

import java.io.File;
import java.util.Optional;


public interface CardRepository {
    CardApplicant save(CardApplicant entity);
    CardApplicant saveForceCommit(CardApplicant entity);
    Optional<CardApplicant> findById(String s);
    Optional<CardApplicant> findByIdPessimisticWrite(String pin);
    Optional<CardApplicant> findByIdPessimisticRead(String pin);
    Boolean existsById(String s);
    void deleteById(String s);

    CardApplicant readFile(File file);
    File writeFile(CardApplicant entity);
    CardApplicant updateFile(CardApplicant entity, File file);
    void deleteFile(File file);
}
