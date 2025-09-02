package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("Создаю фильм с именем : {}", newFilm.getName());
        return filmService.create(newFilm);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Обновляю фильм с именем : {}", film.getName());
        return filmService.update(film);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long filmId) {
        log.info("Запрос на удаление фильма с id={}", filmId);
        filmService.delete(filmId);
        log.info("Фильм с id={} успешно удалён", filmId);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос списка всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос фильма по id={}", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка: filmId={}, userId={}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка: filmId={}, userId={}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") Long count,
                                       @RequestParam(required = false) Long genreId,
                                       @RequestParam(required = false) Long year) {
        if (count != null && count < 0) {
            throw new ValidationException("Количество лайков не может быть отрицательным");
        }

        if (genreId != null || year != null) {
            Long c = (count == null) ? null : count;
            return filmService.getPopular(c, genreId, year);
        } else {
            return filmService.getPopularFilm(count);
        }
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId,
                                           @RequestParam Long friendId) {
        log.info("Запрос общих фильмов: userId={}, friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirectorIdSortedByLikesOrYear(
            @PathVariable Long directorId,
            @RequestParam(required = false, defaultValue = "likes") String sortBy) {

        log.info("Запрос списка фильмов режиссера с сортировкой: directorId={}, sortBy={}", directorId, sortBy);

        String sort = sortBy == null ? "likes" : sortBy.toLowerCase();
        if (!java.util.Set.of("likes", "year").contains(sort)) {
            log.warn("Недопустимое значение параметра sortBy: {}", sortBy);
            throw new ValidationException("Параметр sortBy может принимать значения 'likes' или 'year'");
        }

        return filmService.getFilmsByDirectorId(directorId, sort);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(@RequestParam String query,
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

        if (searchTitle && searchDirector) {
            return filmService.searchFilmsByTitleAndDirector(query);
        } else if (searchTitle) {
            return filmService.searchFilmsByTitle(query);
        } else {
            return filmService.searchFilmsByDirector(query);
        }
    }
}