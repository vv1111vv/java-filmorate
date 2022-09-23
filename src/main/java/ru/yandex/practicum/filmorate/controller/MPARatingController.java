package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.service.AbstractDataService;

@RestController
@RequestMapping("/mpa")
public class MPARatingController extends AbstractController<MPARating> {
    public MPARatingController(AbstractDataService<MPARating> dataService) {
        super(dataService);
    }

    @Override
    public ResponseEntity<MPARating> create(MPARating data) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Override
    public ResponseEntity<MPARating> update(MPARating data) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
