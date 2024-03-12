package rba.zadatak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import rba.zadatak.deserializer.CardApplicantDtoDeserializer;
import rba.zadatak.dto.CardApplicantDto;
import rba.zadatak.exceptions.CardApplicantFileError;
import rba.zadatak.exceptions.InvalidInputException;
import rba.zadatak.exceptions.UserAlreadyExistsException;
import rba.zadatak.mapper.CardApplicantDtoMapper;
import rba.zadatak.entity.CardApplicant;
import rba.zadatak.service.CardApplicantService;

import java.util.Optional;


@RestController
@RequestMapping("/api")
public class Controller {
    CardApplicantService service;
    CentralConfig config;

    public Controller(CardApplicantService service, CentralConfig config) {
        this.service=service;
        this.config=config;
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public String constraintViolation(HttpServletResponse res, ConstraintViolationException e) {
        StringBuilder errorMessages = new StringBuilder();

        res.setStatus(HttpStatus.BAD_REQUEST.value());
        e.getConstraintViolations().forEach(it->{
            errorMessages.append(it.getMessage()).append(",");
        });
        if (errorMessages.length()>0) {
            errorMessages.deleteCharAt(errorMessages.length()-1);
        }

        res.setContentType("text/html; charset=utf-8");
        return String.format("Invalid input provided: [%s]",errorMessages);
    }

    @ExceptionHandler(InvalidInputException.class)
    public String invalidInputExceptionHandler(HttpServletResponse res, InvalidInputException e) {

        StringBuilder errorMessages = new StringBuilder();

        res.setStatus(HttpStatus.BAD_REQUEST.value());

        e.getErrors().forEach(it-> {
            errorMessages.append(it.getDefaultMessage()).append(",");
        });

        if (errorMessages.length() > 0) {
            errorMessages.deleteCharAt(errorMessages.length() - 1);
        }

        res.setContentType("text/html; charset=utf-8");
        return String.format("Invalid input provided: [%s]", errorMessages);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public String userAlreadyExistsException(HttpServletResponse res, UserAlreadyExistsException e) {
        res.setStatus(HttpServletResponse.SC_CONFLICT);
        res.setContentType("text/html; charset=utf-8");
        return e.getMessage();
    }

    @ExceptionHandler(CardApplicantFileError.class)
    public String fileOperationError(HttpServletResponse res) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        res.setContentType("text/html; charset=utf-8");
        return "Unable to process the request due to internal server error";
    }


    @PostMapping("save")
    public ResponseEntity save(@RequestBody CardApplicantDto applicant) {
        CardApplicant cardApplicant = CardApplicantDtoMapper.MAPPER.cardDtoToCard(applicant);
        applicant = CardApplicantDtoMapper.MAPPER.cardToCardDto(service.storeApplicant(cardApplicant));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(applicant);
    }

    @PutMapping("edit")
    public ResponseEntity edit(@RequestBody CardApplicantDto applicant) {
        CardApplicant cardApplicant = CardApplicantDtoMapper.MAPPER.cardDtoToCard(applicant);
        applicant = CardApplicantDtoMapper.MAPPER.cardToCardDto(service.editApplicant(cardApplicant));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(applicant);
    }

    @GetMapping("/find/{pin}")
    public ResponseEntity getApplicant(@PathVariable("pin") String pin) {
        // validiraj?
        Optional<CardApplicant> applicant = service.findApplicant(pin);
        if (applicant.isPresent()) {
            CardApplicantDto cardApplicantDto = CardApplicantDtoMapper.MAPPER.cardToCardDto(applicant.get());
            return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(cardApplicantDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{pin}")
    public ResponseEntity delete(@PathVariable("pin") String pin ) {
        // validiraj??
        service.deleteApplicant(pin);
        return ResponseEntity.ok("User deleted");
    }
}
