package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface FeedRepository {

    public Collection<FeedEvent> findFeedEventsByUserId(Long userId);

    public void saveReview(Review review);

    public void updateReview(Review review);

    public void deleteReview(Review review);

    public void saveFriend(Long userId, Long friendId);

    public void removerFriend(Long userId, Long friendId);

    public void saveLike(Long filmId, Long userId);

    public void removeLike(Long filmId, Long userId);
}