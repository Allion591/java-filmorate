package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = {"userId", "friendId"})
public class Friendship {
    private final long userId;
    private final long friendId;
    private final String status;
}