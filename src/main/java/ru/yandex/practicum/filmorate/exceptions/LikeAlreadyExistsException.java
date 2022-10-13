package ru.yandex.practicum.filmorate.exceptions;

public class LikeAlreadyExistsException extends RuntimeException {

    public LikeAlreadyExistsException(String message) {
        super(message);
    }
}
