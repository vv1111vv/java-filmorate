package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface AbstractFilmStorage extends AbstractDataStorage<Film> {
    void addLike(int id, int userId);

    boolean removeLike(int id, int userId);

    List<Film> getPopularFilms(int count);
}
