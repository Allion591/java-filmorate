package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
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

    @PutMapping
    public User update(@Valid @RequestBody User user) {
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

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получена команда на вывод всех пользователей");
        return users.values();
    }

    private long getNextId() {
        log.info("Генерация id");
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.info("id сгенерирован успешно");
        return ++currentMaxId;
    }
}