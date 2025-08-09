package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUser = new User("valid@email.com", "validLogin",
                LocalDate.of(2000, 1, 1));
    }


    @Test
    void shouldPassValidationWithValidData() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        validUser.setEmail("");
        assertViolationContainsMessage(validUser, "Не указан адрес электронной почты");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        validUser.setEmail("invalid-email");
        assertViolationContainsMessage(validUser, "Неверный формат электронной почты");
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        validUser.setLogin("");
        assertViolationContainsMessage(validUser, "Логин не может быть пустым");
    }

    @Test
    void shouldFailWhenLoginHasSpaces() {
        validUser.setLogin("login with spaces");
        assertViolationContainsMessage(validUser, "Логин не может содержать пробелы");
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        assertViolationContainsMessage(validUser, "Дата рождения не может быть в будущем");
    }

    private void assertViolationContainsMessage(User user, String expectedMessage) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains(expectedMessage)));
    }
}