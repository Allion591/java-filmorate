package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.lang.NonNull;
import ru.yandex.practicum.filmorate.exception.NotFriendException;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

@Data
public class User {
    private Long id;
    private Set<Long> friends = new TreeSet<>();

    private final Friendship friendship;

    @NonNull
    @Email(message = "Неверный формат электронной почты")
    @NotBlank(message = "Не указан адрес электронной почты")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    @NonNull
    private String login;

    private String name;

    @NonNull
    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public void addFriend(long anotherUserId) {
        friends.add(anotherUserId);
    }

    public void removeFriend(long anotherUserId) {
        if (friends.contains(anotherUserId)) {
            friends.remove(anotherUserId);
        } else {
            throw new NotFriendException("Пользователя нет у вас в друзьях");
        }
    }
}