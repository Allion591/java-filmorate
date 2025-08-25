package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.FriendRepository;
import ru.yandex.practicum.filmorate.interfaces.UserRepository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Repository
@Qualifier("jdbcUserRepository")
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {
    private final NamedParameterJdbcOperations jdbcOperations;

    @Qualifier("jdbcFriendRepository")
    private final FriendRepository friendRepository;

    @Override
    public User makeUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User(
                rs.getString("email"),
                rs.getString("login"),
                rs.getDate("birthday").toLocalDate()
        );
        user.setId(rs.getLong("user_id"));
        user.setName(rs.getString("name"));

        friendRepository.loadFriendsForUser(user);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = :email, login = :login, " +
                "name = :name, birthday = :birthday " +
                "WHERE user_id = :userId";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday())
                .addValue("userId", user.getId());

        int updated = jdbcOperations.update(sql, params);
        if (updated == 0) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    @Override
    public void deleteById(Long id) {
        // Удаляем дружбы, где пользователь фигурирует с любой стороны
        String deleteFriendships = "DELETE FROM friendship WHERE user_id = :userId OR friend_id = :userId";
        SqlParameterSource params = new MapSqlParameterSource("userId", id);
        jdbcOperations.update(deleteFriendships, params);

        // Удаляем лайки, поставленные пользователем
        String deleteLikes = "DELETE FROM likes WHERE user_id = :userId";
        jdbcOperations.update(deleteLikes, params);

        // Удаляем самого пользователя
        String deleteUser = "DELETE FROM users WHERE user_id = :userId";
        int deleted = jdbcOperations.update(deleteUser, params);
        if (deleted == 0) {
            throw new NotFoundException("Пользователь с ID=" + id + " не найден");
        }
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcOperations.query(sql, this::makeUser);
    }

    @Override
    public User getUserById(long id) {
        String sql = "SELECT * FROM users WHERE user_id = :user_id";
        SqlParameterSource parameterSource = new MapSqlParameterSource("user_id", id);

        try {
            return jdbcOperations.queryForObject(sql, parameterSource, this::makeUser);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public User save(User user) {
        log.info("Получена команда на создание пользователя {}", user);
        log.info("Пользователь прошел валидацию");
        String sql = "INSERT INTO users (email, login, name, birthday) " +
                "VALUES (:email, :login, :name, :birthday)";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(sql, parameterSource, keyHolder, new String[]{"user_id"});
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Отпрален запрос в БД");
        log.info("Сгенерирован ключ и пользователь сохранен");

        return user;
    }

    @Override
    public Collection<User> findCommonFriends(long userId, long otherUserId) {
        String sql =
                "SELECT u.* FROM users u " +
                        "JOIN friendship f1 ON u.user_id = f1.friend_id " +
                        "JOIN friendship f2 ON u.user_id = f2.friend_id " +
                        "WHERE f1.user_id = :userId AND f2.user_id = :otherUserId";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("otherUserId", otherUserId);

        return jdbcOperations.query(sql, params, this::makeUser);
    }
}