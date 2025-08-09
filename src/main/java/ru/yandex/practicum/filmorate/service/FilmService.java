package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public void delete(Film film) {
        filmRepository.deleteById(film.getId());
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
}