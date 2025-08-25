package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.FeedEvent;
import java.util.Collection;

public interface FeedRepository {
    public Collection<FeedEvent> findFeedEventsByUserId(long userId);
}