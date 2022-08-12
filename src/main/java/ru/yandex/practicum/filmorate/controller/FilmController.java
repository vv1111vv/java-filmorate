package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;

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
    private final FilmService filmService;
    private final UserService userService;
    private static final LocalDate LOCAL_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmController(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    //добавление фильма;
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Фильм: " + film.getName() + " создан с id: " + film.getId());
        validateFilm(film);
        return filmService.create(film);
    }

    // обновление фильма
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: " + film.getName() + " с id: " + film.getId());
        validateFilm(film);
        return filmService.update(film);
    }

    // Получение всех фильмов
    @GetMapping
    public ArrayList getFilms() {
        log.info("Получен запрос на получение списка всех фильмов");
        return filmService.getFilms();
    }
    private void validateFilm(Film film) {  // дата релиза — не раньше 28 декабря 1895 года;
        if(film.getReleaseDate().isBefore(LOCAL_DATE)) {
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}

