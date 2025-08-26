package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.UserRepository;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.repository.JdbcFeedRepository;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final JdbcFeedRepository feedRepository;
    private final UserRepository userRepository;

    public Collection<FeedEvent> getFeedEvents(long userId) {
        userRepository.getUserById(userId);
        return feedRepository.findFeedEventsByUserId(userId);
    }
}