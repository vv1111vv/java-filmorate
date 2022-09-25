package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.Value;


@Value
@Data
@Builder
public class Friendship {
    User user;
    User friend;
}

