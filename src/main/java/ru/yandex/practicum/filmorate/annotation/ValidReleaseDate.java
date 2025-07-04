package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validators.FilmReleaseDateValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FilmReleaseDateValidator.class)
public @interface ValidReleaseDate {
    String message() default "Дата релиза не может быть раньше 28 декабря 1895 года";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}