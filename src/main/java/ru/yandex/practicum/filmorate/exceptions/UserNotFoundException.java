package ru.yandex.practicum.filmorate.exceptions;

public class UserNotFoundException extends NotFoundException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
