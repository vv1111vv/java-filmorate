package ru.yandex.practicum.filmorate.utils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDate;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = IsAfter.DateValidator.class)
@Documented
public @interface IsAfter {
    String message() default "{message.key}";

    String current();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class DateValidator implements ConstraintValidator<IsAfter, LocalDate> {

        private String validDate;

        @Override
        public void initialize(IsAfter constraintAnnotation) {
            validDate = constraintAnnotation.current();
        }

        @Override
        public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
            String[] splitDate = validDate.split("-");
            return date.isAfter(LocalDate.of(Integer.parseInt(splitDate[0]), Integer.parseInt(splitDate[1]),
                    Integer.parseInt(splitDate[2])));
        }
    }
}
