package ru.yandex.practicum.filmorate.storage.database;

import ru.yandex.practicum.filmorate.model.MPARating;

import java.util.List;

public interface MPAStorage {

    List<MPARating> getAllMpa();

    MPARating getMpaById(Long id);
}
