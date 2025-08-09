package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmServiceTest {
    private final FilmService filmService;
    private final UserService userService;

    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;

    @BeforeEach
    void setUp() {
        Mpa mpa = new Mpa(1, "G");
        Genre comedy = new Genre(1, "Комедия");

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(comedy);

        user1 = new User("valid@email.com", "validLogin",
                LocalDate.of(2000, 1, 1));
        user1.setName("TestName");
        user1 = userService.create(user1); // Сохраняем обновленный объект с ID

        film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description Film 1");
        film1.setReleaseDate(LocalDate.of(2001, 12, 29));
        film1.setDuration(Duration.ofMinutes(90));
        film1.setMpa(mpa);
        film1.setGenres(genres);
        film1 = filmService.create(film1); // Сохраняем обновленный объект

        film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description Film 2");
        film2.setReleaseDate(LocalDate.of(1995, 12, 29));
        film2.setDuration(Duration.ofMinutes(120));
        film2.setMpa(mpa);
        film2.setGenres(genres);
        film2 = filmService.create(film2);

        film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description Film 3");
        film3.setReleaseDate(LocalDate.of(2005, 12, 29));
        film3.setDuration(Duration.ofMinutes(30));
        film3.setMpa(mpa);
        film3.setGenres(genres);
        film3 = filmService.create(film3);
    }

    @Test
    void addLike_shouldAddLikeAndValidateUser() {
        filmService.addLike(film1.getId(), user1.getId());
        Film updatedFilm = filmService.getFilmById(film1.getId());
        assertEquals(1, updatedFilm.getLikesCount());
    }

    @Test
    void addLike_shouldThrowExceptionWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> filmService.addLike(film1.getId(), 999L));
    }

    @Test
    void removeLike_shouldRemoveExistingLike() {
        filmService.addLike(film2.getId(), user1.getId());
        filmService.removeLike(film2.getId(), user1.getId());
        Film updatedFilm = filmService.getFilmById(film2.getId());
        assertEquals(0, updatedFilm.getLikesCount());
    }

    @Test
    void removeLike_shouldThrowExceptionWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> filmService.removeLike(film1.getId(), 999L));
    }
}