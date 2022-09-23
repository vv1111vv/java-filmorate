package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.AbstractDataStorage;
import ru.yandex.practicum.filmorate.model.MPARating;

@Service
public class MPARatingService extends AbstractDataService<MPARating> {
    public MPARatingService(AbstractDataStorage<MPARating> storage) {
        super(storage);
    }
}
