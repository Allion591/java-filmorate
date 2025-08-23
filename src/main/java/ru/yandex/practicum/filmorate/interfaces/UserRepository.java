package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface UserRepository {

    public User makeUser(ResultSet rs, int rowNum) throws SQLException;

    public User update(User user);

    public String delete(User user);

    public List<User> findAll();

    public User getUserById(long id);

    public User save(User user);

    public Collection<User> findCommonFriends(long userId, long otherUserId);
}