package ru.yandex.practicum.filmorate.model;

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
