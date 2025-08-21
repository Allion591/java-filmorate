package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

@Slf4j
@Qualifier("jdbcFilmRepository")
@Repository
public class JdbcFilmRepository implements FilmRepository {
    private final NamedParameterJdbcOperations jdbcOperations;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;
    private final DirectorRepository directorRepository;
    private final ResultSetExtractor<Collection<Film>> filmsExtractor;
    private final ResultSetExtractor<Film> filmExtractor;

    @Autowired
    public JdbcFilmRepository(
            NamedParameterJdbcOperations jdbcOperations,
            @Qualifier("jdbcMpaRepository") MpaRepository mpaRepository,
            @Qualifier("jdbcGenreRepository") GenreRepository genreRepository,
            @Qualifier("jdbcLikeRepository") LikeRepository likeRepository,
            @Qualifier("jdbcDirectorRepository") DirectorRepository directorRepository) {

        this.jdbcOperations = jdbcOperations;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.likeRepository = likeRepository;
        this.directorRepository = directorRepository;
        this.filmsExtractor = createFilmsExtractor();
        this.filmExtractor = createFilmExtractor();
    }

    private ResultSetExtractor<Collection<Film>> createFilmsExtractor() {
        return rs -> {
            Map<Long, Film> films = new LinkedHashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Film film = films.get(filmId);
                if (film == null) {
                    film = mapRow(rs);
                    films.put(filmId, film);
                }
                log.info("Добавляю жанры");
                genreRepository.addGenreFromResultSet(rs, film);

                log.info("Добавляю режиссеров");
                directorRepository.addDirectorFromResultSet(rs, film);

                Long userId = rs.getLong("user_id");
                log.info("Ищу лайки");
                if (userId != 0 && !rs.wasNull()) {
                    log.info("Добавляю лайк для фильма {} от пользователя {}", filmId, userId);
                    film.addLike(filmId, userId);
                }
            }
            return new ArrayList<>(films.values());
        };
    }

    private ResultSetExtractor<Film> createFilmExtractor() {
        return rs -> {
            Film film = null;
            while (rs.next()) {
                if (film == null) {
                    film = mapRow(rs);
                }
                genreRepository.addGenreFromResultSet(rs, film);
                directorRepository.addDirectorFromResultSet(rs, film);
            }
            if (film != null) {
                loadAdditionalData(film);
            }
            return film;
        };
    }

    private Film mapRow(ResultSet rs) throws SQLException {
        Film film = new Film();
        log.info("Ищу фильм в mapRow");
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("films_name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(Duration.ofMinutes(rs.getLong("duration")));

        long mpaId = rs.getLong("mpa_id");
        if (mpaId > 0) {
            film.setMpa(mpaRepository.findById(mpaId));
        }
        return film;
    }

    private void loadAdditionalData(Film film) {
        log.info("Запускаю метод loadAdditionalData: {}", film);
        List<Like> likes = likeRepository.findLikesByFilmId(film.getId());
        film.setLikes(new TreeSet<>(Comparator.comparing(Like::getIdUser)));
        film.getLikes().addAll(likes);

        Set<Director> directors = directorRepository.loadDirectors(film.getId().intValue());
        film.setDirectors(directors);
    }


    @Override
    public Film getFilmById(Long id) {
        log.info("Ищем фильм по id: {}", id);
        String sql = "SELECT " +
                "f.film_id, " +
                "f.films_name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "f.mpa_id, " +
                "m.mpa_name, " +
                "g.genre_id, " +
                "g.genre_name, " +
                "d.director_id, " +
                "d.director_name " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_genre fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
                "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE f.film_id = :id";
        Film film = jdbcOperations.query(sql,
                new MapSqlParameterSource("id", id),
                filmExtractor
        );

        if (film == null) {
            throw new NotFoundException("Фильм не найден");
        }
        log.info("Возвращаю фильм : {}", film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Запускаю метод findAll");
        String sql = "SELECT " +
                "f.film_id, " +
                "f.films_name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "f.mpa_id, " +
                "m.mpa_name, " +
                "g.genre_id, " +
                "g.genre_name, " +
                "l.user_id, " +
                "d.director_id, " +
                "d.director_name " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_genre fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                "ORDER BY f.film_id";
        return jdbcOperations.query(sql, filmsExtractor);
    }

    @Override
    public Film save(Film film) {
        mpaRepository.findById(film.getMpa().getId());
        log.info("Сохраняю фильм : {}", film);
        String sql = "INSERT INTO films (films_name, description, release_date, duration, mpa_id) " +
                "VALUES (:films_name, :description, :releaseDate, :duration, :mpaId)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("films_name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration().toMinutes())
                .addValue("mpaId", film.getMpa() != null ? film.getMpa().getId() : null);

        try {
            jdbcOperations.update(sql, params, keyHolder, new String[]{"film_id"});
        } catch (DataAccessException e) {
            log.error("Ошибка при сохранении фильма: {}", film, e);
            throw new DataAccessException("Ошибка сохранения фильма", e) {
            };
        }

        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);

        genreRepository.saveFilmGenres(film);
        directorRepository.saveDirectors(film);
        return getFilmById(filmId);
    }

    @Override
    public Film update(Film film) {
        log.info("Получена команда на обновление фильма {}", film);
        log.info("Фильм успешно прошел валидацию для обновления");

        String sql = "UPDATE films SET " +
                "films_name = :films_name, " +
                "description = :description, " +
                "release_date = :release_date, " +
                "duration = :duration, " +
                "mpa_id = :mpa_id " +
                "WHERE film_id = :film_id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", film.getId())
                .addValue("films_name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("release_date", film.getReleaseDate())
                .addValue("duration", film.getDuration().toMinutes())
                .addValue("mpa_id", film.getMpa() != null ? film.getMpa().getId() : null);

        int updated = jdbcOperations.update(sql, params);
        if (updated == 0) {
            log.warn("Фильм c данным id каталоге не найден {}", film.getId());
            throw new NotFoundException("Фильм с ID=" + film.getId() + " не найден");
        }

        genreRepository.updateFilmGenres(film);
        directorRepository.saveDirectors(film);
        return getFilmById(film.getId());
    }

    @Override
    public void deleteById(Long id) {
        log.info("Удаляю фильм : {}", id);
        String deleteGenresSql = "DELETE FROM film_genre WHERE film_id = :film_id";
        jdbcOperations.update(deleteGenresSql, new MapSqlParameterSource("film_id", id));

        String deleteLikesSql = "DELETE FROM likes WHERE film_id = :id";
        jdbcOperations.update(deleteLikesSql, new MapSqlParameterSource("id", id));

        String deleteDirectorsSql = "DELETE FROM film_directors WHERE film_id = :film_id";
        jdbcOperations.update(deleteDirectorsSql, new MapSqlParameterSource("film_id", id));

        String deleteFilmSql = "DELETE FROM films WHERE film_id = :id";
        int deleted = jdbcOperations.update(deleteFilmSql, new MapSqlParameterSource("id", id));

        if (deleted == 0) {
            throw new NotFoundException("Фильм с ID=" + id + " не найден");
        }
    }

    public Collection<Film> findPopularFilms(Long count) {
        log.info("Получаю популярные фильмы: {}", count);
        String sql = "SELECT " +
                "   f.film_id, " +
                "   f.films_name, " +
                "   f.description, " +
                "   f.release_date, " +
                "   f.duration, " +
                "   f.mpa_id, " +
                "   g.genre_id, " +
                "   g.genre_name, " +
                "   m.mpa_name, " +
                "   l.user_id, " +
                "   d.director_id, " +
                "   d.director_name, " +
                "   COUNT(l.user_id) OVER (PARTITION BY f.film_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN film_genre fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                "ORDER BY likes_count DESC, f.film_id ";

        MapSqlParameterSource params = new MapSqlParameterSource("count", count);
        return jdbcOperations.query(sql, params, filmsExtractor);
    }

    @Override
    public Collection<Film> findCommonFilms(Long userId, Long friendId) {
        String sql = """
                SELECT
                    f.film_id,
                    f.films_name,
                    f.description,
                    f.release_date,
                    f.duration,
                    f.mpa_id,
                    m.mpa_name,
                    g.genre_id,
                    g.genre_name,
                    l.user_id,
                    COUNT(l.user_id) OVER (PARTITION BY f.film_id) AS likes_count
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN film_genre fg ON f.film_id = fg.film_id
                LEFT JOIN genre g ON fg.genre_id = g.genre_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                WHERE EXISTS (
                    SELECT 1 FROM likes l1 WHERE l1.film_id = f.film_id AND l1.user_id = :userId
                )
                AND EXISTS (
                    SELECT 1 FROM likes l2 WHERE l2.film_id = f.film_id AND l2.user_id = :friendId
                )
                ORDER BY likes_count DESC, f.film_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return jdbcOperations.query(sql, params, filmsExtractor);
    }


    @Override
    public Collection<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        log.info("Получаю фильмы по id режиссера : {}", directorId);
        log.info("Сортирую по : {}", sortBy);
        String sql;
        if ("likes".equals(sortBy)) {
            sql = """
                    SELECT
                    f.film_id,
                    f.films_name,
                    f.description,
                    f.release_date,
                    f.duration,
                    f.mpa_id,
                    m.mpa_name,
                    d.director_id,
                    d.director_name,
                    g.genre_id,
                    g.genre_name,
                    l.user_id,
                    COUNT(l.user_id) OVER (PARTITION BY f.film_id) AS likes_count
                    FROM films f
                    LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                    LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.director_id
                    LEFT JOIN film_genre fg ON f.film_id = fg.film_id
                    LEFT JOIN genre g ON fg.genre_id = g.genre_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    WHERE d.director_id = :directorId
                    ORDER BY likes_count DESC
                    """;
        } else if ("year".equals(sortBy)) {
            sql = """
                    SELECT
                    f.film_id,
                    f.films_name,
                    f.description,
                    f.release_date,
                    f.duration,
                    f.mpa_id,
                    m.mpa_name,
                    d.director_id,
                    d.director_name,
                    g.genre_id,
                    g.genre_name,
                    l.user_id AS like_user_id
                    FROM films f
                    LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                    LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.director_id
                    LEFT JOIN film_genre fg ON f.film_id = fg.film_id
                    LEFT JOIN genre g ON fg.genre_id = g.genre_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    WHERE d.director_id = :directorId
                    ORDER BY EXTRACT(YEAR FROM f.release_date)
                    """;
        } else {
            throw new IllegalArgumentException("Неизвестный параметр для сортировки: " + sortBy);
        }
        Map<String, Object> params = Map.of("directorId", directorId);
        return jdbcOperations.query(sql, params, filmsExtractor);
    }
}