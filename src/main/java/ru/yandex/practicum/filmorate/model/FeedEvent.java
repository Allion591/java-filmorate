package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class FeedEvent {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
    private Long timestamp;
}