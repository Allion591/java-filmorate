package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.RecommendationService;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<Collection<Film>> getRecommendations(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getRecommendations(id));
    }
}
