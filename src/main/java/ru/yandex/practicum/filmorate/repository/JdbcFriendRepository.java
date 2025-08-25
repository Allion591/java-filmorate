package ru.yandex.practicum.filmorate.repository;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFriendException;
import ru.yandex.practicum.filmorate.interfaces.FriendRepository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("jdbcFriendRepository")
@RequiredArgsConstructor
public class JdbcFriendRepository implements FriendRepository {
    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public void addFriend(long userId, long friendId) {
        if (!existsById(userId) || !existsById(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        String checkSql = "SELECT COUNT(*) FROM friendship WHERE user_id = :userId AND friend_id = :friendId";
        SqlParameterSource checkParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        Integer count = jdbcOperations.queryForObject(checkSql, checkParams, Integer.class);
        if (count != null && count > 0) {
            throw new DuplicateRequestException("Заявка в друзья уже отправлена");
        }

        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (:userId, :friendId)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        jdbcOperations.update(sql, params);

        String eventSql = "INSERT INTO feed_events (user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES (:userId, 'FRIEND', 'ADD', :friendId, :timestamp)";
        jdbcOperations.update(eventSql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId)
                .addValue("timestamp", System.currentTimeMillis()));
        log.info("Событие добавление друга добавлено: friend_id={}, user_id={}", friendId, userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        if (!existsById(userId) || !existsById(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        String sql = "DELETE FROM friendship WHERE user_id = :userId AND friend_id = :friendId";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        int deletedCount = jdbcOperations.update(sql, params);

        if (deletedCount == 0) {
            throw new NotFriendException("Пользователи не являются друзьями");
        } else {
            String eventSql = "INSERT INTO feed_events (user_id, event_type, operation, entity_id, timestamp) " +
                    "VALUES (:userId, 'FRIEND', 'REMOVE', :friendId, :timestamp)";
            jdbcOperations.update(eventSql, new MapSqlParameterSource()
                    .addValue("userId", userId)
                    .addValue("friendId", friendId)
                    .addValue("timestamp", System.currentTimeMillis()));
            log.info("Событие удаление добавлено: friend_id={}, user_id={}", friendId, userId);
        }
    }

    @Override
    public void updateFriends(User user) {
        String deleteSql = "DELETE FROM friendship " +
                "WHERE user_id = :userId " +
                "AND NOT EXISTS (" +
                "   SELECT 1 FROM friendship f2 " +
                "   WHERE f2.user_id = friend_id " +
                "   AND f2.friend_id = :userId" +
                ")";
        jdbcOperations.update(deleteSql, new MapSqlParameterSource("userId", user.getId()));

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            Set<Long> validFriends = user.getFriends().stream()
                    .filter(this::existsById)
                    .collect(Collectors.toSet());

            if (!validFriends.isEmpty()) {
                String insertSql = "INSERT INTO friendship (user_id, friend_id) VALUES (:userId, :friendId)";
                List<MapSqlParameterSource> batchParams = validFriends.stream()
                        .map(friendId -> new MapSqlParameterSource()
                                .addValue("userId", user.getId())
                                .addValue("friendId", friendId))
                        .toList();

                jdbcOperations.batchUpdate(insertSql, batchParams.toArray(new MapSqlParameterSource[0]));
            }
        }
    }

    @Override
    public boolean existsById(long userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE user_id = :id)";
        return Boolean.TRUE.equals(jdbcOperations.queryForObject(
                sql,
                new MapSqlParameterSource("id", userId),
                Boolean.class
        ));
    }

    @Override
    public List<User> findFriendsByUserId(long userId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = :userId";

        SqlParameterSource params = new MapSqlParameterSource("userId", userId);
        return jdbcOperations.query(sql, params, (rs, rowNum) -> {
            User user = new User(
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getDate("birthday").toLocalDate()
            );
            user.setId(rs.getLong("user_id"));
            user.setName(rs.getString("name"));
            return user;
        });
    }

    @Override
    public void loadFriendsForUser(User user) {
        String friendsSql = "SELECT friend_id FROM friendship WHERE user_id = :user_id";
        SqlParameterSource parameterSource = new MapSqlParameterSource("user_id", user.getId());

        List<Long> friends = jdbcOperations.query(
                friendsSql,
                parameterSource,
                (rsFriends, rowNumFriends) -> rsFriends.getLong("friend_id")
        );
        log.info("id друзей {}", friends);
        user.getFriends().clear();
        user.getFriends().addAll(friends);
    }
}