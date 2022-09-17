package ru.yandex.practicum.filmorate.storage;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.List;

public interface FilmStorage {

    Film createFilm(@Valid @RequestBody Film film);

    Film update(@Valid @RequestBody Film film);

    Film getById(Long id);

    List<Film> getAllFilms();
}
