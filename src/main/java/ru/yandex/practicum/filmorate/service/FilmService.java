package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.AbstractDataStorage;
import ru.yandex.practicum.filmorate.dao.AbstractFilmStorage;
import ru.yandex.practicum.filmorate.dao.impl.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Slf4j
@Service
public class FilmService extends AbstractDataService<Film> {
    private final AbstractFilmStorage filmStorage;

    public FilmService(AbstractDataStorage<Film> storage, FilmStorage filmStorage) {
        super(storage);
        this.filmStorage = filmStorage;
    }

    public void addLike(int id, int userId) {
        try {
            filmStorage.addLike(id, userId);
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new NotFoundException(String.format("Ошибка добавления лайка, id=%d, userId=%d", id, userId));
        }
    }

    public void removeLike(int id, int userId) {
        try {
            if (!filmStorage.removeLike(id, userId)) {
                throw new NotFoundException(String.format("Ошибка удаления лайка, id=%d, userId=%d", id, userId));
            }
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new NotFoundException(String.format("Ошибка удаления лайка, id=%d, userId=%d", id, userId));
        }
    }

    public List<Film> getPopularFilms(int count) {
        try {
            return filmStorage.getPopularFilms(count);
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new NotFoundException(String.format("Ошибка получения %d популярных фильмов", count));
        }
    }
}
