package rba.zadatak.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import jakarta.persistence.Id;
import lombok.Setter;
import rba.zadatak.annotations.NameConstraint;
import rba.zadatak.annotations.WriteToFile;
import rba.zadatak.enums.CardStatus;

@Entity
@Getter
@Setter
public class CardApplicant {
    @Id
    @Pattern(regexp="^\\d{11}$", message="OIB mora imati 11 znamenki")
    @WriteToFile
    private String pin; // OIB

    @NameConstraint(min=2, max=30, message =
            "Ime mora imati između 2 i 30 znakova te mora sadržavati isključivo slova, uključujući vodoravnu crtu")
    @WriteToFile
    private String firstName;

    @NameConstraint(min=2, max=30, message =
            "Prezime mora imati između 2 i 30 znakova te mora sadržavati isključivo slova, uključujući vodoravnu crtu")
    @WriteToFile
    private String lastName;

    // ideja je da prilikom stvaranja nove prijave za karticu njen status uvijek bude PENDING
    @NotNull
    @WriteToFile
    private CardStatus status = CardStatus.PENDING;


    // ime datoteke u koju se ispisuju korisnicki podaci
    private String filename;

    // empty constructor za potrebe frameworka
    public CardApplicant() {}

    // maksimalna velicina u bajtovima
    public int getMaxSizeInBytes() {
        return 100;
    }

    public CardApplicant(String firstName, String lastName, String pin, CardStatus status) {
        this.firstName=firstName;
        this.lastName=lastName;
        this.pin=pin;
        this.status=status;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s", this.pin, this.firstName, this.lastName, this.status);
    }

    @Override
    public boolean equals(Object other) {
        if (other==this) return true;

        if (! (other instanceof CardApplicant)) {
            return false;
        }

        CardApplicant o = (CardApplicant) other;

        if (!this.firstName.equals(o.firstName)) return false;
        if (!this.lastName.equals(o.lastName)) return false;
        if (!this.pin.equals(o.pin)) return false;

        return true;
    }

}
