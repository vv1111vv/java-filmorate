package ru.yandex.practicum.filmorate.exceptions;

public class UserDoesNotExistByIdException extends RuntimeException {


    public UserDoesNotExistByIdException(String message) {
        super(message);
    }
}