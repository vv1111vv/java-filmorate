package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.database.DirectorDao;

public class DirectorValidators {

    private static final String EMPTY_NAME = "Имя режиссёра не может быть пустым.";

    public static String check(Director director) {
        String message = "";
        if (director.getName().isBlank()) {
            message = EMPTY_NAME;
        }
        return message;
    }

    public static void isDirectorExists(DirectorDao storage, Integer directorId,
                                        String message, Logger log) throws ObjectNotFoundException {
        if (storage.find(directorId) == null) {
            log.warn(message);
            throw new ObjectNotFoundException(message);
        }
    }
}
