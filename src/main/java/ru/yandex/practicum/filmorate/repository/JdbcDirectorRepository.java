package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.DirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Qualifier("jdbcDirectorRepository")
@Repository
public class JdbcDirectorRepository implements DirectorRepository {
    private final NamedParameterJdbcOperations jdbc;

    public JdbcDirectorRepository(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_ALL_DIRECTORS = "SELECT * FROM directors";

    private static final String SELECT_FILM_DIRECTORS_SQL =
            "SELECT d.director_id, d.director_name FROM directors d " +
                    "JOIN film_directors fd ON d.director_id = fd.director_id " +
                    "WHERE fd.film_id = :filmId";

    private static final String SELECT_DIRECTOR_BY_ID = "SELECT * FROM directors WHERE director_id = :id";

    @Override
    public void saveDirectors(Film film) {
        log.info("Сохранение режиссеров для фильма ID: {}", film.getId());

        String deleteSql = "DELETE FROM film_directors WHERE film_id = :filmId";
        int deleted = jdbc.update(deleteSql, new MapSqlParameterSource("filmId", film.getId()));
        log.info("Удалено {} связей для фильма ID: {}", deleted, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            log.info("Добавление {} режиссеров для фильма ID: {}", film.getDirectors().size(), film.getId());

            String insertSql = "INSERT INTO film_directors (film_id, director_id) VALUES (:filmId, :directorId)";

            MapSqlParameterSource[] batchParams = film.getDirectors().stream()
                    .map(director -> new MapSqlParameterSource()
                            .addValue("filmId", film.getId())
                            .addValue("directorId", director.getId()))
                    .toArray(MapSqlParameterSource[]::new);

            int[] inserted = jdbc.batchUpdate(insertSql, batchParams);
            log.info("Добавлено {} связей для фильма ID: {}", inserted.length, film.getId());
        } else {
            log.info("Нет режиссеров для сохранения для фильма ID: {}", film.getId());
        }
    }

    @Override
    public Set<Director> loadDirectors(int filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return new LinkedHashSet<>(jdbc.query(SELECT_FILM_DIRECTORS_SQL, params, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getInt("director_id"));
            director.setDirectorName(rs.getString("director_name"));
            return director;
        }));
    }

    @Override
    public void addDirectorFromResultSet(ResultSet rs, Film film) throws SQLException {
        long directorId = rs.getLong("director_id");
        if (directorId != 0 && !rs.wasNull()) {
            Director director = new Director();
            director.setId((int) directorId);
            director.setDirectorName(rs.getString("director_name"));
            film.getDirectors().add(director);
            log.debug("Добавлен режиссер ID: {} к фильму ID: {}", directorId, film.getId());
        }
    }

    @Override
    public List<Director> findAll() {
        log.debug("Возврат всех режиссеров");
        return jdbc.query(SELECT_ALL_DIRECTORS, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getInt("director_id"));
            director.setDirectorName(rs.getString("director_name"));
            return director;
        });
    }

    @Override
    public Optional<Director> findById(Long id) {
        log.debug("Возврат режиссера по id: {}", id);
        List<Director> directors = jdbc.query(SELECT_DIRECTOR_BY_ID,
                new MapSqlParameterSource("id", id),
                (rs, rowNum) -> {
                    Director director = new Director();
                    director.setId(rs.getInt("director_id"));
                    director.setDirectorName(rs.getString("director_name"));
                    return director;
                });
        if (directors.isEmpty()) {
            log.info("Режиссер не найден: {}", id);
            throw new NotFoundException("Режиссер не найден");
        } else {
            return Optional.of(directors.getFirst());
        }
    }

    @Override
    public Director createDirector(Director director) {
        String sql = "INSERT INTO directors (director_name) VALUES (:name)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", director.getDirectorName());

        jdbc.update(sql, params, keyHolder, new String[]{"director_id"});

        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        log.info("Режиссер добавлен в базу: {}", director);
        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET director_name = :name WHERE director_id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", director.getDirectorName())
                .addValue("id", director.getId());

        int updated = jdbc.update(sql, params);
        if (updated == 0) {
            throw new NotFoundException("Режиссер с ID=" + director.getId() + " не найден");
        }

        return director;
    }

    @Override
    public void deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE director_id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int deleted = jdbc.update(sql, params);
        if (deleted == 0) {
            throw new NotFoundException("Режиссер с ID=" + id + " не найден");
        }
    }
}