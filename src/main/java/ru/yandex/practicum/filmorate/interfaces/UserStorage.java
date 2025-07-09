package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {
    
    public User create(User newUser);

    public User update(User user);

    public User delete(User user);
}
