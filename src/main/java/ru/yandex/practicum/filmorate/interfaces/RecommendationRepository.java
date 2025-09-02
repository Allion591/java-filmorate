package ru.yandex.practicum.filmorate.interfaces;

import java.util.List;

public interface RecommendationRepository {
    List<Long> findRecommendedFilmIdsForUser(Long userId);
}
