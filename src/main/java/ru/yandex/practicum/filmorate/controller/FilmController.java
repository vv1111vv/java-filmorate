package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
Класс контроллер описывает эндпоинты для класса Film:
 - добавление фильма;
 - обновление фильма;
 - получение всех фильмов.
 */
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    public void validateFilm(Film film) {  // дата релиза — не раньше 28 декабря 1895 года;
        if(film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        }
    }
    //добавление фильма;
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Фильм: " + film.getName() + " создан с id: " + film.getId());
        validateFilm(film);
        return create(film);
    }

    // обновление фильма
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: " + film.getName() + " с id: " + film.getId());
        validateFilm(film);
        return update(film);
    }

    // Получение всех фильмов
    @GetMapping
    public ArrayList getFilms() {
        log.info("Получен запрос на получение списка всех фильмов");
        return getFilms();
    }

}

