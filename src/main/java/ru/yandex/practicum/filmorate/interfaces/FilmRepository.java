package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmRepository {


    public Film update(Film film);

    public Collection<Film> findAll();

    public Film getFilmById(Long id);

    public Film save(Film film);

    public void deleteById(Long id);

    public Collection<Film> findPopularFilms(Long count);

    public Collection<Film> findCommonFilms(Long userId, Long friendId);


    public Collection<Film> getFilmsByDirectorId(int directorId, String sortBy);
}