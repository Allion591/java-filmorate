package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;
import ru.yandex.practicum.filmorate.service.DurationSetup;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @NotBlank(message = "Описание фильма не может быть пустым")
    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @ValidReleaseDate(message = "Дата релиза должна быть не раньше")
    private LocalDate releaseDate;

    @NotNull(message = "Не введена продолжительность")
    @JsonSerialize(using = DurationSetup.DurationSerializer.class)
    @JsonDeserialize(using = DurationSetup.DurationDeserializer.class)
    private Duration duration;

    @AssertTrue(message = "Продолжительность должна быть положительной")
    private boolean isDurationPositive() {
        return duration != null && !duration.isNegative() && !duration.isZero();
    }
}