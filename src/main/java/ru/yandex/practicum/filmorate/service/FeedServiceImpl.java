package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.FeedRepository;
import ru.yandex.practicum.filmorate.interfaces.FeedService;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Review;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private final FeedRepository feedRepository;

    @Override
    public Collection<FeedEvent> getFeedEvents(Long userId) {
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
    public void saveFriend(Long userId, Long friendId) {
        feedRepository.saveFriend(userId, friendId);
    }

    @Override
    public void removerFriend(Long userId, Long friendId) {
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