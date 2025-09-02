package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final FilmService filmService;

    @GetMapping(value = {"/{id}"})
    public Mpa getMpaNameById(@PathVariable Long id) {
        return filmService.getMpaNameById(id);
    }

    @GetMapping
    public Collection<Mpa> mpaGetAll() {
        return filmService.mpaGetAll();
    }
}