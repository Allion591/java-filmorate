package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("Получена команда на добавление фильма {}", newFilm);
        log.info("Фильм успешно прошел валидацию");
        newFilm.setId(getNextId());

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм успешно сохранен");
        return newFilm;
    }

    private long getNextId() {
        long newId = nextId.getAndIncrement();
        log.info("Генерация id для фильма: {}", newId);
        return newId;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получена команда на обновление фильма {}", film);

        log.info("Фильм успешно прошел валидацию для обновления");
        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());
            log.info("Фильм найден в памяти");

            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());
            log.info("Все поля успешно изменены {}", oldFilm);
            return oldFilm;
        }
        log.warn("Фильм c данным id каталоге не найден {}", film.getId());
        throw new NotFoundException("Фильм с названием = " + film.getName() + " не найден");
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получена команда на получение всех фильмов");
        return films.values();
    }
}