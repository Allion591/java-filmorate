package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class Review {
    private int reviewId;
    private String content;
    private Boolean isPositive;
    private Integer useful;
    private Long userId;
    private Long filmId;
}
