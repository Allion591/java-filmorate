package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> create(@Valid @RequestBody User newUser) {
        return new ResponseEntity<>(userService.create(newUser), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        return new ResponseEntity<>(userService.update(user), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Collection<User>> findAll() {
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = {"/{id}"})
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
    }

    @PutMapping("{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return ResponseEntity.ok(Map.of("message", userService.addFriend(id, friendId)));
    }

    @DeleteMapping("{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return ResponseEntity.ok(Map.of("message", userService.removeFriend(id, friendId)));
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<User>> getFriends(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFriends(id));
    }

    @GetMapping("{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> getMutualFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return new ResponseEntity<>(userService.getMutualFriends(id, otherId), HttpStatus.OK);
    }
}