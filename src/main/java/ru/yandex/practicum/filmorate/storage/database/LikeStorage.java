package ru.yandex.practicum.filmorate.storage.database;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.Collection;
import java.util.List;

public interface LikeStorage {
    List<Film> getPopularFilms(int limit);

    void saveLike(Like like);

    void deleteLike(Like like);

    List<Film> findAll();
}

