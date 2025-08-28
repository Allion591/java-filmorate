package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> findAll() {
        log.info("Запрос всех режиccёров");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Long id) {
        log.info("Получение режиссера по ID: {}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Создание нового режиссера: {}", director.getDirectorName());
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Обновление режиссера с ID: {}", director.getId());
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirector(@PathVariable Long id) {
        log.info("Удаление режиссера с ID: {}", id);
        directorService.deleteDirector(id);
    }
}