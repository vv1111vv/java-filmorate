package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationErrorResponse;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.AbstractData;
import ru.yandex.practicum.filmorate.service.AbstractDataService;

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
public abstract class AbstractController<T extends AbstractData> {

    private final AbstractDataService<T> dataService;

    public AbstractController(AbstractDataService<T> dataService) {
        this.dataService = dataService;
    }

    @GetMapping
    public ResponseEntity<Iterable<T>> findAll() {
        return new ResponseEntity<>(dataService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> findById(@PathVariable int id) {
        Optional<T> optional = dataService.findById(id);
        if (optional.isEmpty()) {
            throw new NotFoundException(String.format("Объект с id=%s не найден", id));
        }

        log.info("Объект с id={} успешно найден", id);
        return ResponseEntity.ok(optional.get());
    }

    @PostMapping
    public ResponseEntity<T> create(@Valid @RequestBody T data) {
        Optional<T> optional = dataService.create(data);
        if (optional.isEmpty()) {
            throw new ValidationException(String.format("Объект класса %s не создан",
                    data.getClass().getSimpleName()));
        }

        log.info("Объект класса {} успешно создан", data.getClass().getSimpleName());
        return ResponseEntity.ok(optional.get());

    }

    @PutMapping
    public ResponseEntity<T> update(@Valid @RequestBody T data) {
        Optional<T> optional = dataService.update(data);
        if (optional.isEmpty()) {
            throw new NotFoundException(String.format("Объект с id=%s класса %s не удалось обновить",
                    data.getId(), data.getClass().getSimpleName()));
        }
        log.info("Объект с id={} класса {} успешно обновлен",
                data.getId(), data.getClass().getSimpleName());
        return ResponseEntity.ok(optional.get());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(new ValidationErrorResponse(e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorResponse.ValidationError(error.getField(), error.getDefaultMessage()))
                .peek(error -> log.warn(error.getMessage()))
                .collect(Collectors.toList())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<Map<String, String>> handleException(ValidationException e) {
        log.warn(e.getMessage());

        return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Map<String, String>> handleException(NotFoundException e) {
        log.warn(e.getMessage());

        return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
    }
}
