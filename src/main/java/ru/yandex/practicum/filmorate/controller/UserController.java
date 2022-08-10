package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;


/*
Класс контроллер описывает эндпоинты для класса User:
 - создание пользователя;
 - обновление пользователя;
 - получение списка всех пользователей;
 */

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Создать пользователя
    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);
        log.info("Запрос на добавление пользователя " + user.getName() + " id " + user.getId() + " получен");
        return userService.create(user);
    }

    //Обновить пользователя
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Запрос на обновление пользователя " + user.getName() + " id " + user.getId() + " получен");
        long userId = user.getId();
        if (userId < 0 && !userService.getAllUsers().contains(user)) {
            throw new NotFoundObjectException("Пользователя с id " + user.getId() + " нет");
        }
        validateUser(user);
        return userService.update(user);
    }

    //полученить список всех пользователей
    @GetMapping
    public ArrayList<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    public void validateUser(User user) {
        //  электронная почта не может быть пустой и должна содержать символ @;
        String userEmail = user.getEmail();
        boolean mailFormat = userEmail.contains("@");
        if (userEmail.isEmpty() || !mailFormat) {
            log.info( "Почта для пользователя " + user.getName() + " с id: " + user.getId() + " имеет ошибку");
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
        // логин не может быть пустым и содержать пробелы;
        String userLogin = user.getLogin();
        boolean loginFormat = userLogin.contains(" ");
        if (userLogin.isBlank() || loginFormat) {
            log.info("Логин имеет ошибку у пользователя " + user.getName() + " c id " + user.getId());
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        //  имя для отображения может быть пустым — в таком случае будет использован логин;
        String userName = user.getName();
        if(userName.isEmpty()) {
            user.setName(userLogin);
        }
        // дата рождения не может быть в будущем.
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("дата рождения не может быть в будущем");
        }
    }
}

