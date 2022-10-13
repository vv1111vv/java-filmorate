package ru.yandex.practicum.filmorate.exceptions;

public abstract class NotFoundException extends Exception{
    public NotFoundException(String message) {
        super(message);
    }
}
