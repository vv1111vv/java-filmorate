package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.FilmService;


import java.util.List;

@RestController
@RequestMapping(value = "/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final FilmService filmService;

    @GetMapping
    public List<Director> findAll() {
        return filmService.findAllDirectors();
    }

    @GetMapping("/{id}")
    public Director findById( @PathVariable("id") int directorId) throws ObjectNotFoundException {
        return filmService.findDirectorById(directorId);
    }

    @PostMapping
    public Director addDirector( @RequestBody Director director) throws ValidationException {
        return filmService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector( @RequestBody Director director) throws ValidationException, ObjectNotFoundException {
        return filmService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int directorId) throws ObjectNotFoundException {
        filmService.deleteDirector(directorId);
    }

}
