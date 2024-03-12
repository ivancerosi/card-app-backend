package rba.zadatak.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import rba.zadatak.annotations.NameConstraint;


public class NameValidator implements ConstraintValidator<NameConstraint, String> {

    private int MAX = Integer.MAX_VALUE;
    private int MIN = Integer.MIN_VALUE;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        if (name==null || name.isEmpty()) return false;

        if (name.length()>MAX || name.length()<MIN) return false;

        for (char c : name.toCharArray()) {
            if (!Character.isLetter(c) && !Character.isWhitespace(c) && c != '-') {
                return false;
            }
        }
        return true;
    }


    @Override
    public void initialize(NameConstraint nameConstraint) {
        MIN = nameConstraint.min();
        MAX = nameConstraint.max();
    }

}
