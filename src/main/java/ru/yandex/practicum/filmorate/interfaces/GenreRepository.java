package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

public interface GenreRepository {

    public Genre findById(Long genreId);

    public Collection<Genre> findAll();

    public void validateGenres(Set<Genre> genre);

    public void addGenreFromResultSet(ResultSet rs, Film film) throws SQLException;

    public void saveFilmGenres(Film film);

    public void updateFilmGenres(Film film);
}