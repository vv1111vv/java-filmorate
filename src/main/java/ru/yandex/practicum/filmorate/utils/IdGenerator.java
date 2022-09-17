package ru.yandex.practicum.filmorate.utils;

public class IdGenerator {
    private static long userId = 0;
    private static long filmId = 0;

    public static long nextUserId() {
        return ++userId;
    }

    public static long nextFilmId() {
        return ++filmId;
    }
}
