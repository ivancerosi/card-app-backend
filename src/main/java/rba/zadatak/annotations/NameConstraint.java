package rba.zadatak.annotations;

import jakarta.validation.Constraint;
import rba.zadatak.validator.NameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NameValidator.class)
public @interface NameConstraint {
    String message() default "rba.zadatak.annotations.constraint illegal name";
    Class<?>[] groups() default { };

    Class<? extends String>[] payload() default { };

    int min() default 0;
    int max() default 0;

}
