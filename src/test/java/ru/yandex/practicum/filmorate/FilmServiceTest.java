package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {
    private UserStorage storage = new InMemoryUserStorage();
    private final UserService userService = new UserService(storage);

    private FilmStorage filmStorage = new InMemoryFilmStorage();
    private FilmService filmService = new FilmService(filmStorage, userService);


    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;

    @BeforeEach
    void setUp() {
        user1 = new User("valid@email.com", "validLogin",
                LocalDate.of(2000, 1, 1));
        userService.create(user1);

        film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description Film 1");
        film1.setReleaseDate(LocalDate.of(2001, 12, 29));
        film1.setDuration(Duration.ofMinutes(90));
        filmService.create(film1);

        film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description Film 2");
        film2.setReleaseDate(LocalDate.of(1995, 12, 29));
        film2.setDuration(Duration.ofMinutes(120));
        filmService.create(film2);

        film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description Film 3");
        film3.setReleaseDate(LocalDate.of(2005, 12, 29));
        film3.setDuration(Duration.ofMinutes(30));
        filmService.create(film3);
    }

    @Test
    void addLike_shouldAddLikeAndValidateUser() {
        filmService.addLike(film1.getId(), user1.getId());
        assertEquals(1, film1.getLikesCount());
    }

    @Test
    void addLike_shouldAddLikeAndNotValidateUser() {

        assertThrows(NotFoundException.class, () -> filmService.addLike(film1.getId(), 2L));
    }

    @Test
    void removeLike_shouldRemoveExistingLike() {
        filmService.addLike(film2.getId(), user1.getId());
        filmService.removeLike(film2.getId(), user1.getId());

        assertEquals(0, film1.getLikesCount());
    }

    @Test
    void removeLike_shouldIgnoreNonExistingLike() {
        assertThrows(NotFoundException.class, () -> filmService.removeLike(film1.getId(), 3L));
    }

    @Test
    void showGetPopularFilm() {
        filmService.addLike(film2.getId(), user1.getId());
        Collection<Film> popularFilm = filmService.getPopularFilm(3L);
        List<Film> list = new ArrayList<>(popularFilm);

        assertEquals(1, list.get(0).getLikesCount());
        assertEquals(0, list.get(1).getLikesCount());
        assertEquals(0, list.get(2).getLikesCount());
    }
}