package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public User create(User newUser) {
        log.info("Получена команда на создание пользователя {}", newUser);
        log.info("Пользователь прошел валидацию");
        newUser.setId(getNextId());
        log.info("Присвоен id {}", newUser.getId());

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Проверка на наличие имени {}", newUser.getName());
            newUser.setName(newUser.getLogin());
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь сохранен {}", newUser);
        return newUser;
    }

    @Override
    public User update(User user) {
        log.info("Получена команда на обновление пользователя {}", user);
        log.info("Пользователь прошел валидацию для обновления");
        if (users.containsKey(user.getId())) {
            User oldUser = users.get(user.getId());
            log.info("Данные получены из памяти");
            if (user.getName() == null || user.getName().isBlank()) {
                log.info("Проверка имени для обновления {}", user.getName());
                user.setName(user.getLogin());
            }
            oldUser.setName(user.getName());
            oldUser.setLogin(user.getLogin());
            oldUser.setEmail(user.getEmail());
            oldUser.setBirthday(user.getBirthday());

            log.info("Все поля обновлены");
            return oldUser;
        }
        log.warn("Пользователь с id {} не найден", user.getId());
        throw new NotFoundException("Пользователь с именем = " + user.getName() + " не найден");
    }

    @Override
    public String delete(User user) {
        if (users.containsKey(user.getId())) {
            users.remove(user.getId());
            return "Пользователь удален";
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public Collection<User> findAll() {
        log.info("Получена команда на вывод всех пользователей");
        return users.values();
    }

    @Override
    public User getUserById(long id) {
        return Optional.ofNullable(users.get(id))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private long getNextId() {
        long newId = nextId.getAndIncrement();
        log.info("Генерация id для пользователя: {}", newId);
        return newId;
    }
}