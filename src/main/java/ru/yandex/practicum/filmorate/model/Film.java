package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;
import ru.yandex.practicum.filmorate.service.DurationSetup;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Film {
    private Set<Long> likeUsersIds = new TreeSet<>();
    private Long id;
    private Long likesCount = 0L;

    private Mpa mpa;

    private Set<Genre> genre;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @ValidReleaseDate(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @NotNull(message = "Не введена продолжительность")
    @JsonSerialize(using = DurationSetup.DurationSerializer.class)
    @JsonDeserialize(using = DurationSetup.DurationDeserializer.class)
    private Duration duration;

    @AssertTrue(message = "Продолжительность должна быть положительной")
    private boolean isDurationPositive() {
        return duration != null && !duration.isNegative() && !duration.isZero();
    }

    @AssertTrue(message = "Недопустимый MPA рейтинг")
    private boolean isValidMpa() {
        return mpa != null && mpa.getId() >= 1 && mpa.getId() <= 5;
    }

    public void setGenres(Set<Genre> genres) {
        this.genre = genres != null ?
                genres.stream()
                        .filter(genre -> genre != null && genre.getId() != 0)
                        .collect(Collectors.toSet()) :
                new HashSet<>();
    }

    public Set<Genre> getGenres() {
        return new HashSet<>(genre);
    }
}