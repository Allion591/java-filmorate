package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {

    public Film create(Film newFilm);

    public Film update(Film film);

    public Film delete(Film film);
}
