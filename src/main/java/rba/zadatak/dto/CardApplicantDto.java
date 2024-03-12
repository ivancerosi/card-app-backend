package rba.zadatak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import rba.zadatak.annotations.NameConstraint;
import rba.zadatak.enums.CardStatus;


/*
** Data Transfer Object kojeg Jackson automatski deserijalizira iz JSON-a u Java objekt.
 */

@Data
public class CardApplicantDto {
    @Id
    @Pattern(regexp="\\d{11}", message="OIB mora imati 11 znamenki")
    private String pin; // OIB

    @NameConstraint(min=2, max=30, message =
            "Ime mora imati između 2 i 30 znakova te mora sadržavati isključivo slova, uključujući vodoravnu crtu")
    private String firstName;

    @NameConstraint(min=2, max=30, message =
            "Prezime mora imati između 2 i 30 znakova te mora sadržavati isključivo slova, uključujući vodoravnu crtu")
    private String lastName;

    @Nullable
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private CardStatus status=CardStatus.PENDING;


    public CardApplicantDto() {}

}
