package rba.zadatak.service;

import rba.zadatak.entity.CardApplicant;

import java.util.Optional;


public interface CardApplicantService {
    CardApplicant storeApplicant(CardApplicant cardApplicant);
    void deleteApplicant(String id);
    Optional<CardApplicant> findApplicant(String pin);
    CardApplicant editApplicant(CardApplicant cardApplicant);
}
