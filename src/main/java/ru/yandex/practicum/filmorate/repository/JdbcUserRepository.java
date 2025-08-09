package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public class UserRepository implements ru.yandex.practicum.filmorate.interfaces.UserRepository {
    @Override
    public User create(User newUser){

    };

    @Override
    public User update(User user){};

    @Override
    public String delete(User user){};

    @Override
    public Collection<User> findAll(){};

    @Override
    public User getUserById(long id){};
}