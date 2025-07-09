package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage inMemoryUserStorage;

    @Autowired
    public UserService(UserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public String addFriend(Long userId, Long anotherUserId) {
        User user = inMemoryUserStorage.getUserById(userId);
        User anotherUser = inMemoryUserStorage.getUserById(anotherUserId);
        log.info("Добавление друга");
        user.addFriend(anotherUser.getId());
        anotherUser.addFriend(user.getId());
        return "Пользователь " + anotherUser.getName() + " теперь ваш друг!";
    }

    public String removeFriend(Long userId, Long anotherUserId) {
        User user = inMemoryUserStorage.getUserById(userId);
        User anotherUser = inMemoryUserStorage.getUserById(anotherUserId);
        log.info("Удаление друга");
        user.removeFriend(anotherUser.getId());
        anotherUser.removeFriend(user.getId());
        return "Пользователь " + anotherUser.getName() + " теперь вам не друг";
    }

    public Collection<User> getFriends(Long userId) {
        User targetUser = inMemoryUserStorage.getUserById(userId);
        Set<Long> friendIds = targetUser.getFriends();

        log.info("Получение списка всех друзей");
        return inMemoryUserStorage.findAll().stream()
                .filter(user -> friendIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    public Collection<User> getMutualFriends(Long userId, Long anotherUserId) {
        User user = inMemoryUserStorage.getUserById(userId);
        User anotherUser = inMemoryUserStorage.getUserById(anotherUserId);
        log.info("Получение списка общих друзей");
        return user.getFriends().stream()
                .filter(anotherUser.getFriends()::contains)
                .map(inMemoryUserStorage::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void checkUser(Long userId) {
        log.info("Проверка на наличие пользователя");
        if (inMemoryUserStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public User create(User newUser) {
        return inMemoryUserStorage.create(newUser);
    }

    public User update(User user) {
        return inMemoryUserStorage.update(user);
    }

    public String delete(User user) {
        return inMemoryUserStorage.delete(user);
    }

    public Collection<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    public User getUserById(long id) {
        return inMemoryUserStorage.getUserById(id);
    }
}