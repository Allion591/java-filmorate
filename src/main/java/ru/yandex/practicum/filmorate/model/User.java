package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NonNull
    @Email(message = "Неверный формат электронной почты")
    @NotBlank(message = "Не указан адрес электронной почты")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    @NonNull
    private String login;

    private String name;

    @NonNull
    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}