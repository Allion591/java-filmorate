package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final FilmService filmService;

    @GetMapping(value = {"/{id}"})
    public ResponseEntity<Genre> getGenreNameById(@PathVariable Long id) {
        return new ResponseEntity<>(filmService.getGenreNameById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Collection<Genre>> genreGetAll() {
        return new ResponseEntity<>(filmService.genreGetAll(), HttpStatus.OK);
    }
}