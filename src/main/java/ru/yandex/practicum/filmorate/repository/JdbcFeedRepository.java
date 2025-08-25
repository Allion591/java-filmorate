package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.FeedRepository;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class JdbcFeedRepository implements FeedRepository {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Collection<FeedEvent> findFeedEventsByUserId(long userId) {
        String sql = "SELECT * FROM feed_events " +
                "WHERE user_id = :userId " +
                "OR (event_type = 'FRIEND' AND entity_id = :userId) " +
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
}