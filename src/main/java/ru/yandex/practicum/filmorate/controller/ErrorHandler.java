package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.AllOtherException;
import ru.yandex.practicum.filmorate.exception.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exception.ValidationException;


import java.util.Map;

/*
Класс содержит методы обработки исключений
 */
@RestControllerAdvice(assignableTypes = {FilmController.class, UserController.class})
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleValidation(final ValidationException v){
        log.warn(v.getMessage());
        return new ResponseEntity<>(
                Map.of("Проверка выполнена неправильно ", v.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleNotFound(final NotFoundObjectException n){
        log.warn(n.getMessage());
        return new ResponseEntity<>(
                Map.of("Объект не найден ", n.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleAllOther(final AllOtherException a){
        log.warn(a.getMessage());
        return new ResponseEntity<>(
                Map.of("Внимание ", a.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
