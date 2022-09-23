package ru.yandex.practicum.filmorate.model.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDateConstraint, LocalDate> {
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        final LocalDate EARLY_DATE = LocalDate.of(1895, 12, 28);
        return localDate.isAfter(EARLY_DATE);
    }
}
