package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.interfaces.DirectorRepository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorRepository directorRepository;

    @GetMapping
    public Collection<Director> findAll() {
        log.debug("Запрос всех режиccёров");
        return directorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Director>> getDirectorById(@PathVariable Long id) {
        log.info("Получение режиссера по ID: {}", id);
        return new ResponseEntity<>(directorRepository.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Director> createDirector(@Valid @RequestBody Director director) {
        log.info("Создание нового режиссера: {}", director.getDirectorName());
        return new ResponseEntity<>(directorRepository.createDirector(director), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody Director director) {
        log.info("Обновление режиссера с ID: {}", director.getId());
        getDirectorById((long) director.getId());
        return new ResponseEntity<>(directorRepository.update(director), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirector(@PathVariable int id) {
        log.info("Удаление режиссера с ID: {}", id);
        directorRepository.deleteDirector(id);
    }
}