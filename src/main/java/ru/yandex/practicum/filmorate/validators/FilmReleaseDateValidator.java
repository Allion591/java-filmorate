package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;

import java.time.LocalDate;

public class FilmReleaseDateValidator implements ConstraintValidator<ValidReleaseDate, LocalDate> {
    private static final LocalDate MIN_FILM_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(MIN_FILM_DATE);
    }
}