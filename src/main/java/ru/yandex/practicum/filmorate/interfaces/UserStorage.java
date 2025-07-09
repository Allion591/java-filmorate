package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    public User create(User newUser);

    public User update(User user);

    public String delete(User user);

    public Collection<User> findAll();

    public User getUserById(long id);
}