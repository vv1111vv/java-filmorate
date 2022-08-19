package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

/*
Класс контроллер описывает эндпоинты для класса Film:
 - добавление фильма;
 - обновление фильма;
 - получение всех фильмов.
 - добавить/удалить likes
 - получить список Топ-10 фильмов
 */
@Validated
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    private static final LocalDate LOCAL_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
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

    //получить фильм по id
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        log.info("Получен запрос на поиск фильма с id: " + id);
        return filmService.getFilmById(id);
    }

    //поставить like от пользователя по id
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос на лайк для фильма с id " + id);
        filmService.addLike(id, userId);
    }

    //удалить like от пользователя по id
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос на удаление лайка для фильма с id " + id);
        filmService.deleteLike(id, userId);
    }

    //получить список из 10-Топ фильмов
    @GetMapping("/popular")
    public Set<Film> topFilm(@Positive @RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на список популярных фильмов");
        return filmService.topFilm(count);
    }

    private void validateFilm(Film film) {  // дата релиза — не раньше 28 декабря 1895 года;
        if(film.getReleaseDate().isBefore(LOCAL_DATE)) {
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}

