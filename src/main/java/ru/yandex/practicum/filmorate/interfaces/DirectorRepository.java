package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface DirectorRepository {
    void saveDirectors(Film film);

    Set<Director> loadDirectors(int filmId);

    void addDirectorFromResultSet(ResultSet rs, Film film) throws SQLException;

    Collection<Director> findAll();

    Optional<Director> findById(Long id);

    Director createDirector(Director director);

    Director update(Director director);

    void deleteDirector(int id);
}