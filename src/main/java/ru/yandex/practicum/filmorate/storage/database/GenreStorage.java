package ru.yandex.practicum.filmorate.storage.database;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {

    List<Genre> getAll();

    Genre get(Long id);
}