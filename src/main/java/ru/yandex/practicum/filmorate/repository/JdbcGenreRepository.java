package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.GenreRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("jdbcGenreRepository")
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public Genre findById(Long genreId) {
        String sql = "SELECT * FROM genre WHERE genre_id = :genre_id";
        log.info("Собираю жанры");
        try {
            return jdbcOperations.queryForObject(
                    sql,
                    new MapSqlParameterSource("genre_id", genreId),
                    (rs, rowNum) -> new Genre(
                            rs.getLong("genre_id"),
                            rs.getString("genre_name"))
            );
        } catch (EmptyResultDataAccessException e) {
            log.info("Поймал ошибку " + e.getMessage());
            throw new NotFoundException("Один или несколько жанров не найдены");
        }
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genre ORDER BY genre_id";
        return jdbcOperations.query(sql, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("genre_name")));
    }

    @Override
    public void validateGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        log.info("Валидирую жанры: {}", genreIds);

        String sql = "SELECT COUNT(*) FROM genre WHERE genre_id IN (:genre_id)";
        try {
            Integer count = jdbcOperations.queryForObject(
                    sql,
                    new MapSqlParameterSource("genre_id", genreIds),
                    Integer.class
            );

            if (count == null) {
                log.warn("COUNT(*) вернул null для жанров: {}", genreIds);
                count = 0;
            }

            if (count != genreIds.size()) {
                throw new NotFoundException("Один или несколько жанров не существуют");
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка при валидации жанров: {}", genreIds, e);
            throw new NotFoundException("Ошибка при проверке жанров");
        }
    }

    @Override
    public void addGenreFromResultSet(ResultSet rs, Film film) throws SQLException {
        long genreId = rs.getLong("genre_id");
        boolean isGenreIdNull = rs.wasNull();

        if (isGenreIdNull || genreId <= 0) {
            return;
        }

        String genreName = rs.getString("genre_name");
        if (genreName == null) {
            return;
        }

        Genre genre = new Genre(genreId, genreName);
        log.info("Добавляю жанр {} в фильм {}", genre.getName(), film.getId());
        film.addGenres(genre);
    }

    @Override
    public void saveFilmGenres(Film film) {
        log.info("Сохраняю жанры фмльма");
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        validateGenres(film.getGenres());
        log.info("Жанры прошли валидацию");
        Set<Genre> uniqueGenres = new LinkedHashSet<>(film.getGenres());
        Set<Long> existingIds = getExistingGenreIds(film.getId());
        List<Genre> newGenres = uniqueGenres.stream()
                .filter(genre -> !existingIds.contains(genre.getId()))
                .toList();

        if (newGenres.isEmpty()) return;

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (:film_id, :genre_id)";

        List<MapSqlParameterSource> batchParams = newGenres.stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("film_id", film.getId())
                        .addValue("genre_id", genre.getId()))
                .toList();

        try {
            log.info("Отпраляю запрос на сохранение жанров");
            jdbcOperations.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
        } catch (DataAccessException e) {
            log.error("Ошибка при сохранении жанров для фильма ID={}", film.getId(), e);
            throw new DataAccessException("Ошибка сохранения жанров фильма", e) {
            };
        }
    }

    @Override
    public void updateFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genre WHERE film_id = :film_id";
        jdbcOperations.update(deleteSql, new MapSqlParameterSource("film_id", film.getId()));
        saveFilmGenres(film);
    }

    private Set<Long> getExistingGenreIds(Long filmId) {
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = :film_id";
        return new LinkedHashSet<>(jdbcOperations.queryForList(
                sql,
                new MapSqlParameterSource("film_id", filmId),
                Long.class
        ));
    }
}