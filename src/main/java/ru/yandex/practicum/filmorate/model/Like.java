package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = {"idFilm, idUser"})
public class Like {
    private final long idFilm;
    private final long idUser;
}