package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Film create(Film newFilm);

    public Film update(Film film);

    public Collection<Film> findAll();

    public void delete(Film film);

    public Film getFilmById(Long id);
}