package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;
import ru.yandex.practicum.filmorate.service.DurationSetup;
import java.time.*;
import java.util.*;

@Slf4j
@Data
public class Film {
    private Set<Like> likes = new TreeSet<>(Comparator.comparing(Like::getIdUser));
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long likesCount;
    private Long id;
    private Mpa mpa;
    private Set<Genre> genres = new LinkedHashSet<>();

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

    public Set<Genre> getGenres() {
        return Collections.unmodifiableSet(genres);
    }

    public void addGenres(Genre genre) {
        if (genre != null && genre.getId() != 0) {
            this.genres.add(genre);
        }
    }

    public Long getLikesCount() {
        return (long) likes.size();
    }

    public void addLike(Long filmId, Long userId) {
        if (userId != null && userId != 0) {
            likes.add(new Like(filmId, userId));
        }
    }
}