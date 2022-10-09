package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Event {
    private long eventId;            //primary key
    private long userId;
    private String eventType;   // одно из значениий LIKE, REVIEW или FRIEND
    private String operation;   // одно из значениий REMOVE, ADD, UPDATE
    private long entityId;      // идентификатор сущности, с которой произошло событие
    private long timestamp;
}
