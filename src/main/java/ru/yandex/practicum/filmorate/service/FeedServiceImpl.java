package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.FeedService;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.JdbcFeedRepository;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private final JdbcFeedRepository feedRepository;

    @Override
    public Collection<FeedEvent> getFeedEvents(long userId) {
        return feedRepository.findFeedEventsByUserId(userId);
    }

    @Override
    public void saveReview(Review review) {
        feedRepository.saveReview(review);
    }

    @Override
    public void updateReview(Review review) {
        feedRepository.updateReview(review);
    }

    @Override
    public void deleteReview(Review review) {
        feedRepository.deleteReview(review);
    }

    @Override
    public void saveFriend(long userId, long friendId) {
        feedRepository.saveFriend(userId, friendId);
    }

    @Override
    public void removerFriend(long userId, long friendId) {
        feedRepository.removerFriend(userId, friendId);
    }

    @Override
    public void saveLike(Long filmId, Long userId) {
        feedRepository.saveLike(filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        feedRepository.removeLike(filmId, userId);
    }
}