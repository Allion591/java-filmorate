package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.interfaces.UserRepository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.JdbcFriendRepository;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final JdbcFriendRepository friendRepository;

    @Autowired
    public UserService(UserRepository userRepository, JdbcFriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    public void addFriend(Long userId, Long anotherUserId) {
        log.info("Добавление друга");
        friendRepository.addFriend(userId, anotherUserId);
    }

    public void removeFriend(Long userId, Long anotherUserId) {
        friendRepository.removeFriend(userId, anotherUserId);
    }

    public Collection<User> getFriends(Long userId) {
        return friendRepository.findFriendsByUserId(userId);
    }

    public Collection<User> getMutualFriends(Long userId, Long anotherUserId) {
    return userRepository.findCommonFriends(userId, anotherUserId);
    }

    public User create(User newUser) {
        return userRepository.save(newUser);
    }

    public User update(User user) {
        friendRepository.updateFriends(user);
        return userRepository.update(user);
    }

    @Transactional
    public void delete(Long userId) {
        log.info("Удаление пользователя с id={}", userId);
        userRepository.deleteById(userId);
        log.info("Пользователь с id={} удалён", userId);
    }

    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    public User getUserById(long id) {
        return userRepository.getUserById(id);
    }
}