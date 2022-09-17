package ru.yandex.practicum.filmorate.storage.database;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.Collection;

public interface LikeStorage {
    Collection<Film> getPopularFilms(int limit);

    void saveLike(Like like);

    void deleteLike(Like like);
}

