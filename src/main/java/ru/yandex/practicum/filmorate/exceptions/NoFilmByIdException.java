package ru.yandex.practicum.filmorate.exceptions;

public class NoFilmByIdException extends RuntimeException {
    public NoFilmByIdException(String message) {
        super(message);
    }
}
