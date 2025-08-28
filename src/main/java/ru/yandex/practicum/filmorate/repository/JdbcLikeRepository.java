package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.FeedService;
import ru.yandex.practicum.filmorate.interfaces.LikeRepository;
import ru.yandex.practicum.filmorate.model.Like;
import java.util.List;

@Slf4j
@Repository
@Qualifier("jdbcLikeRepository")
@RequiredArgsConstructor
public class JdbcLikeRepository implements LikeRepository {
    private final NamedParameterJdbcOperations jdbcOperations;
    private final FeedService feedService;

    @Override
    public List<Like> findLikesByFilmId(Long filmId) {
        String sql = "SELECT film_id, user_id FROM likes WHERE film_id = :filmId";

        return jdbcOperations.query(
                sql,
                new MapSqlParameterSource("filmId", filmId),
                (rs, rowNum) -> new Like(
                        rs.getLong("film_id"),
                        rs.getLong("user_id")
                )
        );
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String checkLikeSql = "SELECT COUNT(*) FROM likes WHERE film_id = :film_id AND user_id = :user_id";
        Integer likeCount = jdbcOperations.queryForObject(
                checkLikeSql,
                new MapSqlParameterSource()
                        .addValue("film_id", filmId)
                        .addValue("user_id", userId),
                Integer.class
        );
        if (likeCount > 0) {
            feedService.saveLike(filmId, userId);
            return;
        } else {
            String sql = "INSERT INTO likes (film_id, user_id) VALUES (:film_id, :user_id)";
            jdbcOperations.update(
                    sql,
                    new MapSqlParameterSource()
                            .addValue("film_id", filmId)
                            .addValue("user_id", userId)
            );
            log.info("Лайк добавлен: film_id={}, user_id={}", filmId, userId);
            feedService.saveLike(filmId, userId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = :film_id AND user_id = :user_id";
        int rowsUpdated = jdbcOperations.update(sql, new MapSqlParameterSource()
                .addValue("film_id", filmId)
                .addValue("user_id", userId));

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
        feedService.removeLike(filmId, userId);
    }

    @Override
    public Long getLikesCountForFilm(Long filmId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = :film_id";
        return jdbcOperations.queryForObject(
                sql,
                new MapSqlParameterSource("film_id", filmId),
                Long.class
        );
    }
}