package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.AbstractDataService;

@RestController
@RequestMapping("/genres")
public class GenreController extends AbstractController<Genre> {
    public GenreController(AbstractDataService<Genre> dataService) {
        super(dataService);
    }

    @Override
    public ResponseEntity<Genre> create(Genre data) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Override
    public ResponseEntity<Genre> update(Genre data) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
