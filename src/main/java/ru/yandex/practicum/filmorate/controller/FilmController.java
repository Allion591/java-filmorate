package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.response.MessageResponse;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film newFilm) {
        return new ResponseEntity<>(filmService.create(newFilm), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        return new ResponseEntity<>(filmService.update(film), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse> delete(Film film) {
        filmService.delete(film);
        return ResponseEntity.ok(new MessageResponse("Фильм " + film.getName() + " удален"));
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> findAll() {
        return new ResponseEntity<>(filmService.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = {"/{id}"})
    public ResponseEntity<Film> getFilmById(@PathVariable Long id) {
        return new ResponseEntity<>(filmService.getFilmById(id), HttpStatus.OK);
    }

    @PutMapping("{id}/like/{userId}")
    public ResponseEntity<MessageResponse> addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.ok(new MessageResponse("Спасибо за оценку."));
    }

    @DeleteMapping("{id}/like/{userId}")
    public ResponseEntity<MessageResponse> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok(new MessageResponse("Лайк удален"));
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopular(@RequestParam(required = false) Long count) {
        if (count != null && count < 0) {
            throw new ValidationException("Количество лайков не может быть отрицательным");
        } else {
            return new ResponseEntity<>(filmService.getPopularFilm(count), HttpStatus.OK);
        }
    }
}