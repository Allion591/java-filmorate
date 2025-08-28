package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.FilmRepository;
import ru.yandex.practicum.filmorate.interfaces.RecommendationRepository;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;

    public Collection<Film> getRecommendations(Long userId) {
        List<Long> filmIds = recommendationRepository.findRecommendedFilmIdsForUser(userId);
        List<Film> result = new ArrayList<>();
        for (Long id : filmIds) {
            result.add(filmRepository.getFilmById(id));
        }
        return result;
    }
}
