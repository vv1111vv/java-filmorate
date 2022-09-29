package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.database.MPADbStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MpaService {
    private final MPADbStorage storage;

    public MpaService(MPADbStorage storage) {
        this.storage = storage;
    }

    public List<MPARating> getAllMpa() {
        return storage.getAllMpa();
    }

    public MPARating getMpaById(Long id) {
        MPARating ratingMPA = storage.getMpaById(id);
        if (ratingMPA == null) throw new NoSuchElementException();
        return ratingMPA;
    }
}
