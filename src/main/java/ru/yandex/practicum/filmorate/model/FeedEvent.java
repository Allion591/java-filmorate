package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class FeedEvent {
    private long eventId;
    private long userId;
    private EventType eventType;
    private Operation operation;
    private long entityId;
    private long timestamp;
}