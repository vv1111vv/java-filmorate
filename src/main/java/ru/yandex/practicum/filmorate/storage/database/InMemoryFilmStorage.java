package ru.yandex.practicum.filmorate.storage.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.controllers.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private long lastFilmId = 0;
    private static HashMap<Long, Film> films = new HashMap<>();
    private final static LocalDate DATE_BORN_MOVIE = LocalDate.of(1895, Month.DECEMBER, 28);
    private final static Logger log = LoggerFactory.getLogger(FilmController.class);

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(long id) throws ObjectNotFoundException {
        if (!films.containsKey(id)) {
            throw new ObjectNotFoundException("Фильм не найден!");
        }
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Сохранён фильм: {}", film.toString());
        return film;
    }

    @Override
    public Film put(Film film) throws ObjectNotFoundException {
        if (!films.containsKey(film.getId())) {
            throw new ObjectNotFoundException("Фильм не найден.");
        }
        films.put(film.getId(), film);
        log.debug("Обновлён фильм: {}", film.toString());
        return film;
    }

    @Override
    public void deleteAll() {
        films.clear();
    }

    @Override
    public void delete(long id) {
        log.debug("Удалён фильм: {}", id);
        films.remove(id);
    }

    @Override
    public boolean addLike(long filmId, long userId) {
        return false;
    }

    @Override
    public boolean deleteLike(long filmId, long userId) {
        return false;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return null;
    }

    @Override
    public MPARating findMpaById(long id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<MPARating> findAllMpa() {
        return null;
    }

    @Override
    public Genre findGenreById(long id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<Genre> findAllGenre() {
        return null;
    }

    @Override
    public List<Film> findFilmsOfDirectorSortByYear(int directorId) {
        return null;
    }

    @Override
    public List<Film> findFilmsOfDirectorSortByLikes(int directorId) {
        return null;
    }


    //ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
    private String check(Film film) throws ValidationException {
        String message = "";
        if (film == null) {
            message = "Данные о фильме не заполнены.";
        } else if (film.getName() == null || film.getName().isBlank()) {
            message = "Название фильма не может быть пустым.";
        } else if (film.getDescription().length() > 200) {
            message = "Превышена максимальная длина описания — 200 символов";
        } else if (film.getReleaseDate().isBefore(DATE_BORN_MOVIE)) {
            message = "Дата релиза не может быть раньше даты 28.12.1895";
        } else if (film.getDuration() <= 0) {
            message = "Продолжительность фильма должна быть больше 0";
        }
        return message;
    }

    private long getNextId() {
        return ++lastFilmId;
    }
}