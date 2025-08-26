package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.FeedRepository;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import java.util.Collection;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcFeedRepository implements FeedRepository {
    private final NamedParameterJdbcOperations jdbc;
    private static final String eventSql = "INSERT INTO feed_events (user_id, event_type, operation, entity_id, " +
            "timestamp) VALUES (:userId, 'REVIEW', 'ADD', :reviewId, :timestamp)";
    private static final String eventSqlForUpdate = "INSERT INTO feed_events (user_id, event_type, operation, " +
            "entity_id, timestamp) VALUES (:userId, 'REVIEW', 'UPDATE', :reviewId, :timestamp)";
    private static final String eventSqlForDelete = "INSERT INTO feed_events (user_id, event_type, operation, " +
            "entity_id, timestamp) VALUES (:userId, 'REVIEW', 'REMOVE', :reviewId, :timestamp)";

    @Override
    public Collection<FeedEvent> findFeedEventsByUserId(long userId) {
        String sql = "SELECT * FROM feed_events " +
                "WHERE user_id = :userId " +
                "OR (event_type IN ('LIKE', 'REVIEW') AND user_id IN (" +
                "    SELECT friend_id FROM friendship WHERE user_id = :userId" +
                "    UNION " +
                "    SELECT user_id FROM friendship WHERE friend_id = :userId" +
                ")) " +
                "ORDER BY timestamp ASC";

        return jdbc.query(sql, new MapSqlParameterSource("userId", userId), (rs, rowNum) -> {
            FeedEvent event = new FeedEvent();
            event.setEventId(rs.getLong("event_id"));
            event.setUserId(rs.getLong("user_id"));
            event.setEventType(EventType.valueOf(rs.getString("event_type")));
            event.setOperation(Operation.valueOf(rs.getString("operation")));
            event.setEntityId(rs.getLong("entity_id"));
            event.setTimestamp(rs.getLong("timestamp"));
            return event;
        });
    }

    @Override
    public void saveReview(Review review) {
        jdbc.update(eventSql, new MapSqlParameterSource()
                .addValue("userId", review.getUserId())
                .addValue("reviewId", review.getReviewId())
                .addValue("timestamp", System.currentTimeMillis()));
        log.info("Событие добавление отзыва добавлено: review_id={}, user_id={}", review.getReviewId(),
                review.getUserId());
    }

    @Override
    public void updateReview(Review review) {
        jdbc.update(eventSqlForUpdate, new MapSqlParameterSource()
                .addValue("userId", review.getUserId())
                .addValue("reviewId", review.getReviewId())
                .addValue("timestamp", System.currentTimeMillis()));
    }

    @Override
    public void deleteReview(Review review) {
        jdbc.update(eventSqlForDelete, new MapSqlParameterSource()
                .addValue("userId", review.getUserId())
                .addValue("reviewId", review.getReviewId())
                .addValue("timestamp", System.currentTimeMillis()));
        log.info("Событие удление отзыва добавлено: review_id={}, user_id={}", review.getReviewId(),
                review.getUserId());
    }

    @Override
    public void saveFriend(long userId, long friendId) {
        String eventSql = "INSERT INTO feed_events (user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES (:userId, 'FRIEND', 'ADD', :friendId, :timestamp)";
        jdbc.update(eventSql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId)
                .addValue("timestamp", System.currentTimeMillis()));
        log.info("Событие добавление друга добавлено: friend_id={}, user_id={}", friendId, userId);
    }

    @Override
    public void removerFriend(long userId, long friendId) {
        String eventSql = "INSERT INTO feed_events (user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES (:userId, 'FRIEND', 'REMOVE', :friendId, :timestamp)";
        jdbc.update(eventSql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId)
                .addValue("timestamp", System.currentTimeMillis()));
        log.info("Событие удаление добавлено: friend_id={}, user_id={}", friendId, userId);
    }

    public void saveLike(Long filmId, Long userId) {
        String eventSql = "INSERT INTO feed_events (user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES (:userId, 'LIKE', 'ADD', :filmId, :timestamp)";
        jdbc.update(eventSql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("filmId", filmId)
                .addValue("timestamp", System.currentTimeMillis()
                ));
        log.info("Событие добаваление лайка добавлено: film_id={}, user_id={}", filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        String eventSql = "INSERT INTO feed_events (user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES (:userId, 'LIKE', 'REMOVE', :filmId, :timestamp)";
        jdbc.update(eventSql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("filmId", filmId)
                .addValue("timestamp", System.currentTimeMillis()));
        log.info("Событие удаление лайка добавлено: film_id={}, user_id={}", filmId, userId);
    }
}