package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.DurationSetup;
import java.time.*;
import java.util.*;

@Data
public class Film {
    private Set<Long> likeUsersIds = new TreeSet<>();
    private Long id;
    private Long likesCount = 0L;

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

    public void addIdUserLike(Long id) {
        if (!likeUsersIds.contains(id)) {
            likeUsersIds.add(id);
            likesCount = (long) likeUsersIds.size();
        } else {
            throw new NotFoundException("Вы уже оценивали фильм");
        }
    }

    public void removeLike(Long id) {
        if (likeUsersIds.contains(id)) {
            likeUsersIds.remove(id);
            likesCount = (long) likeUsersIds.size();
        } else {
            throw new NotFoundException("Вы не оценивали фильм");
        }
    }
}