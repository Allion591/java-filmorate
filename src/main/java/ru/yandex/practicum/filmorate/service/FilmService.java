package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage inMemoryFilmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage inMemoryFilmStorage, UserService userService) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.userService = userService;
    }

    public void addLike(Long filmId, Long userId) {
        userService.checkUser(userId);
        log.info("Добавление лайка");
        Film film = inMemoryFilmStorage.getFilmById(filmId);
        film.addIdUserLike(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = inMemoryFilmStorage.getFilmById(filmId);
        log.info("Удаление лайка");
        film.removeLike(userId);
    }

    public Collection<Film> getPopularFilm(Long count) {
        long limit = (count == null) ? 10 : count;

        log.info("Получение популярных фильмов в количестве: " + limit);
        return inMemoryFilmStorage.findAll().stream()
                .sorted(Comparator.comparing(
                        Film::getLikesCount,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Film create(Film newFilm) {
        return inMemoryFilmStorage.create(newFilm);
    }

    public Film update(Film film) {
        return inMemoryFilmStorage.update(film);
    }

    public void delete(Film film) {
        inMemoryFilmStorage.delete(film);
    }

    public Collection<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        return inMemoryFilmStorage.getFilmById(id);
    }
}