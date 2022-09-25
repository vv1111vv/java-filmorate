package ru.yandex.practicum.filmorate.controllers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistByIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@Data
public class FilmController {
    private final FilmService service;

    @GetMapping("/films")
    public List<Film> getFilms() {
        return service.getAll();
    }

    @PostMapping("/films")
    public Film createFilm(@Valid @RequestBody Film film) {
        service.createFilm(film);
        return film;
    }

    @PutMapping("/films")
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        return film.getId() < 1 ? new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(service.update(film), HttpStatus.OK);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public Film doLike(@PathVariable Long id, @PathVariable Long userId) {
        service.saveLike(id, userId);
        return service.getById(id);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void unLike(@PathVariable Long id, @PathVariable Long userId) {
        service.deleteLike(id, userId);
    }

    @GetMapping("/films/popular")
    public Collection<Film> getPopular(@Positive @RequestParam (value = "count", defaultValue = "10") final int count) {
        return service.getPopularFilms(count);
    }

    @GetMapping("/films/{id}")
    public ResponseEntity<Film> getById(@PathVariable Long id) {
        return id < 1 ? new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<String> exc(ConstraintViolationException ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

