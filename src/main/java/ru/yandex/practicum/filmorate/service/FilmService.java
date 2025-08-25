package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.interfaces.FilmRepository;
import ru.yandex.practicum.filmorate.interfaces.GenreRepository;
import ru.yandex.practicum.filmorate.interfaces.LikeRepository;
import ru.yandex.practicum.filmorate.interfaces.MpaRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.JdbcLikeRepository;
import ru.yandex.practicum.filmorate.repository.JdbcMpaRepository;
import java.util.Collection;

@Slf4j
@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserService userService;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository, UserService userService, JdbcMpaRepository mpaRepository,
                       GenreRepository genreRepository, JdbcLikeRepository likeRepository) {
        this.filmRepository = filmRepository;
        this.userService = userService;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.likeRepository = likeRepository;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка");
        likeRepository.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка");
        likeRepository.removeLike(filmId, userId);
    }

    public Collection<Film> getPopularFilm(Long count) {
        return filmRepository.findPopularFilms(count);
    }

    public Film create(Film newFilm) {
        return filmRepository.save(newFilm);
    }

    public Film update(Film film) {
        return filmRepository.update(film);
    }

    @Transactional
    public void delete(Long filmId) {
        log.info("Удаление фильма с id={}", filmId);
        filmRepository.deleteById(filmId);
        log.info("Фильм с id={} удалён из репозитория", filmId);
    }

    public Collection<Film> findAll() {
        return filmRepository.findAll();
    }

    public Film getFilmById(Long id) {
        return filmRepository.getFilmById(id);
    }

    public Mpa getMpaNameById(Long id) {
        return mpaRepository.findById(id);
    }

    public Collection<Mpa> mpaGetAll() {
        return mpaRepository.mpaGetAll();
    }

    public Genre getGenreNameById(Long id) {
        return genreRepository.findById(id);
    }

    public Collection<Genre> genreGetAll() {
        return genreRepository.findAll();
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("Запрос общих фильмов пользователей {} и {}", userId, friendId);
        return filmRepository.findCommonFilms(userId, friendId);
    }

    public Collection<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        return filmRepository.getFilmsByDirectorId(directorId, sortBy);
    }

    public Collection<Film> searchFilmsByTitle(String query) {
        return filmRepository.searchFilmsByTitle(query);
    }

    public Collection<Film> searchFilmsByDirector(String query) {
        return filmRepository.searchFilmsByDirector(query);
    }

    public Collection<Film> searchFilmsByTitleAndDirector(String query) {
        return filmRepository.searchFilmsByTitleAndDirector(query);
    }
}