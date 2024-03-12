package rba.zadatak.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CardApplicantFileError extends RuntimeException {
    public CardApplicantFileError() {}

    public CardApplicantFileError(String message, Throwable cause) {
        super(message,cause);
    }

    public CardApplicantFileError(String message) {
        super(message);
    }

}
