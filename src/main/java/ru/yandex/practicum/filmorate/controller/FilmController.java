package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.response.MessageResponse;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film newFilm) {
        log.info("Создаю фильм : {}", newFilm);
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.create(newFilm));
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        log.info("Обновляю фильм: {}", film);
        return ResponseEntity.ok(filmService.update(film));
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long filmId) {
        log.info("Запрос на удаление фильма с id={}", filmId);
        filmService.delete(filmId);
        log.info("Фильм с id={} успешно удалён", filmId);
        return ResponseEntity.ok(new MessageResponse("Фильм с id=" + filmId + " удален"));
    }


    @GetMapping
    public ResponseEntity<Collection<Film>> findAll() {
        log.info("Запрос списка всех фильмов");
        return ResponseEntity.ok(filmService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable Long id) {
        log.info("Запрос фильма по id={}", id);
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<MessageResponse> addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка: filmId={}, userId={}", id, userId);
        filmService.addLike(id, userId);
        return ResponseEntity.ok(new MessageResponse("Спасибо за оценку."));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<MessageResponse> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка: filmId={}, userId={}", id, userId);
        filmService.removeLike(id, userId);
        return ResponseEntity.ok(new MessageResponse("Лайк удален"));
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopular(@RequestParam(required = false) Long count,
                                                       @RequestParam(required = false) Integer genreId,
                                                       @RequestParam(required = false) Integer year) {
        if (count != null && count < 0) {
            throw new ValidationException("Количество лайков не может быть отрицательным");
        }
        Collection<Film> result;
        if (genreId != null || year != null) {
            Integer c = (count == null) ? null : count.intValue();
            result = filmService.getPopular(c, genreId, year);
        } else {
            result = filmService.getPopularFilm(count);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/common")
    public ResponseEntity<Collection<Film>> getCommonFilms(@RequestParam Long userId,
                                                           @RequestParam Long friendId) {
        log.info("Запрос общих фильмов: userId={}, friendId={}", userId, friendId);
        Collection<Film> films = filmService.getCommonFilms(userId, friendId);
        return ResponseEntity.ok(films);
    }


    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<Film>> getFilmsByDirectorIdSortedByLikesOrYear(
            @PathVariable int directorId,
            @RequestParam(required = false, defaultValue = "likes") String sortBy) {

        log.info("Запрос списка фильмов режиссера с сортировкой: directorId={}, sortBy={}", directorId, sortBy);

        String sort = sortBy == null ? "likes" : sortBy.toLowerCase();
        if (!java.util.Set.of("likes", "year").contains(sort)) {
            log.warn("Недопустимое значение параметра sortBy: {}", sortBy);
            throw new ValidationException("Параметр sortBy может принимать значения 'likes' или 'year'");
        }

        Collection<Film> films = filmService.getFilmsByDirectorId(directorId, sort);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<Film>> searchFilms(@RequestParam String query,
                                        @RequestParam(required = false, defaultValue = "title,director") String by) {
        log.info("Поиск фильмов по запросу: '{}' , критерии: {}", query, by);

        java.util.Set<String> fields = java.util.Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toSet());
        boolean searchTitle = fields.contains("title");
        boolean searchDirector = fields.contains("director");

        if (!searchTitle && !searchDirector) {
            searchTitle = true;
            searchDirector = true;
        }

        Collection<Film> result;
        if (searchTitle && searchDirector) {
            result = filmService.searchFilmsByTitleAndDirector(query);
        } else if (searchTitle) {
            result = filmService.searchFilmsByTitle(query);
        } else {
            result = filmService.searchFilmsByDirector(query);
        }
        return ResponseEntity.ok(result);
    }
}