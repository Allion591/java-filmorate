package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmTest {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 29));
        validFilm.setDuration(Duration.ofMinutes(90));
    }

    @Test
    void shouldPassValidationWithValidData() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        validFilm.setName("");
        assertViolationContainsMessage(validFilm, "Название фильма не может быть пустым");
    }

    @Test
    void shouldFailWhenDescriptionIsBlank() {
        validFilm.setDescription("");
        assertViolationContainsMessage(validFilm, "Описание фильма не может быть пустым");
    }

    @Test
    void shouldFailWhenDescriptionExceeds200Chars() {
        String longDescription = "a".repeat(201);
        validFilm.setDescription(longDescription);
        assertViolationContainsMessage(validFilm, "Максимальная длина описания - 200 символов");
    }

    @Test
    void shouldFailWhenReleaseDateIsBeforeMin() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .contains("Дата релиза должна быть не раньше")),
                "Не найдено ожидаемое сообщение об ошибке");
    }

    @Test
    void shouldFailWhenDurationIsNull() {
        validFilm.setDuration(null);
        assertViolationContainsMessage(validFilm, "Не введена продолжительность");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        validFilm.setDuration(Duration.ofMinutes(-1));
        assertViolationContainsMessage(validFilm, "Продолжительность должна быть положительной");
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        validFilm.setDuration(Duration.ofMinutes(0));
        assertViolationContainsMessage(validFilm, "Продолжительность должна быть положительной");
    }

    private void assertViolationContainsMessage(Film film, String expectedMessage) {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains(expectedMessage)));
    }
}