package rba.zadatak.exceptions;

import lombok.Getter;
import org.springframework.validation.ObjectError;

import java.util.List;

@Getter
public class InvalidInputException extends RuntimeException {
    List<ObjectError> errors;

    public InvalidInputException(List<ObjectError> errors) {
        super();
        this.errors=errors;
    }
}
