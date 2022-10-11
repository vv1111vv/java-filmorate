package ru.yandex.practicum.filmorate.controllers;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@Valid @PathVariable("id") long id) throws ObjectNotFoundException {
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.create(film);
    }

    @PutMapping
    public Film put(@Valid @RequestBody Film film) throws ValidationException, ObjectNotFoundException {
        return filmService.put(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable("id") long id, @PathVariable("userId") long userId) throws ObjectNotFoundException {
        return filmService.addLike(id,userId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable("filmId") long filmId) throws ObjectNotFoundException, ValidationException {
        filmService.delete(filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable("id") long id, @PathVariable("userId") long userId) throws ObjectNotFoundException {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) throws ValidationException {
        if (count <= 0) {
            throw new ValidationException("Значение параметра count не может быть отрицательно!");
        }
        return filmService.getPopularFilms(count);
    }

    //films/director/{directorId}?sortBy=[year,likes]
    @GetMapping("/director/{directorId}")
    public List<Film> findFilmsDirectorSort(@PathVariable int directorId,
                                            @RequestParam String sortBy) throws ObjectNotFoundException {
        return filmService.findFilmsDirectorSort(directorId, sortBy);
    }

    //метод для тестов
    public void deleteAll() {
        filmService.deleteAll();
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam String query,
                             @RequestParam List<String> by) {
        return filmService.search(query, by);
    }
}

