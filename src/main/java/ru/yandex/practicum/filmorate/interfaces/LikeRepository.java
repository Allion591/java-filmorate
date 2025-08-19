package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Like;

import java.util.List;

public interface LikeRepository {
    public List<Like> findLikesByFilmId(Long filmId);

    public void addLike(Long filmId, Long userId);

    public void removeLike(Long filmId, Long userId);

    public Long getLikesCountForFilm(Long filmId);
}