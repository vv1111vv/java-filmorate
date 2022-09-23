package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface AbstractGenreStorage extends AbstractDataStorage<Genre> {
    List<Genre> findByIds(List<Integer> ids);

    List<Genre> findByFilm(int id);

    void removeByFilm(int id);
}
