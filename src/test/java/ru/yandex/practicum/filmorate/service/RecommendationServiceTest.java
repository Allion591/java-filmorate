package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.interfaces.FilmRepository;
import ru.yandex.practicum.filmorate.interfaces.RecommendationRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    RecommendationRepository recommendationRepository;

    @Mock
    FilmRepository filmRepository;

    @InjectMocks
    RecommendationService recommendationService;

    private Film film(long id, String name) {
        Film f = new Film();
        f.setId(id);
        f.setName(name);
        f.setDescription("d");
        f.setReleaseDate(LocalDate.of(2010,1,1));
        f.setDuration(Duration.ofMinutes(100));
        f.setMpa(new Mpa(1L, "G"));
        return f;
    }

    @Test
    void getRecommendations_emptyIds_returnsEmpty() {
        long userId = 10L;
        when(recommendationRepository.findRecommendedFilmIdsForUser(userId)).thenReturn(List.of());
        Collection<Film> out = recommendationService.getRecommendations(userId);
        assertNotNull(out);
        assertTrue(out.isEmpty());
        verifyNoInteractions(filmRepository);
    }

    @Test
    void getRecommendations_fetchesFilmsInOrder() {
        long userId = 5L;
        List<Long> ids = List.of(3L, 1L, 2L);
        when(recommendationRepository.findRecommendedFilmIdsForUser(userId)).thenReturn(ids);
        when(filmRepository.getFilmById(3L)).thenReturn(film(3L, "F3"));
        when(filmRepository.getFilmById(1L)).thenReturn(film(1L, "F1"));
        when(filmRepository.getFilmById(2L)).thenReturn(film(2L, "F2"));

        Collection<Film> out = recommendationService.getRecommendations(userId);
        List<Film> asList = new ArrayList<>(out);

        assertEquals(3, asList.size());
        assertEquals(3L, asList.get(0).getId());
        assertEquals(1L, asList.get(1).getId());
        assertEquals(2L, asList.get(2).getId());

        verify(filmRepository).getFilmById(3L);
        verify(filmRepository).getFilmById(1L);
        verify(filmRepository).getFilmById(2L);
        verifyNoMoreInteractions(filmRepository);
    }

    @Test
    void getRecommendations_propagatesRepositoryErrors() {
        long userId = 7L;
        when(recommendationRepository.findRecommendedFilmIdsForUser(userId)).thenReturn(List.of(42L));
        when(filmRepository.getFilmById(42L)).thenThrow(new RuntimeException("boom"));
        assertThrows(RuntimeException.class, () -> recommendationService.getRecommendations(userId));
    }
}
