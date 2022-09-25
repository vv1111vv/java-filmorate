package ru.yandex.practicum.filmorate.storage;


import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;

public interface FilmStorage {

    Film createFilm(Film film);

    Film update(Film film);

    Film getById(Long id);

    List<Film> getAllFilms();

    void deleteFilm(Film film);
}
