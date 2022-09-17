package ru.yandex.practicum.filmorate.exceptions;

public class NoFilmByIdException extends Throwable {
    public NoFilmByIdException(String message) {
        super(message);
    }
}
