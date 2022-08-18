package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


/*
Класс контроллер описывает эндпоинты для класса User:
 - создание пользователя;
 - обновление пользователя;
 - получение списка всех пользователей;
 - добавить/удалить пользователя в друзья;
 - получить список обзих друзей
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
    public User create(@Valid @RequestBody User user) {
        User newUser = userService.create(user);
        validateUser(user);
        log.info("Запрос на добавление пользователя " + user.getName() + " id " + user.getId() + " получен");
        return newUser;
    }

    //Обновить пользователя
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        User updatedUser = userService.update(user);
        log.info("Запрос на обновление пользователя " + user.getName() + " id " + user.getId() + " получен");
        validateUser(user);
        return updatedUser;
    }

    //полученить список всех пользователей
    @GetMapping
    public ArrayList<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    //получить пользователя по id
    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        log.info("Получен запрос на поиск пользователя с id: " + id);
        return userService.getUserById(id);
    }

    //добавить пользователя в друзья
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriends(@PathVariable long id, @PathVariable long friendId) {
        log.info("Запрос на добавление пользователя с id " + id + " в друзья с пользователем id " + friendId);
        userService.addFriends(id, friendId);
    }

    //удалить пользователя из друзей
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Запрос на удаление пользователя с id " + friendId + " из друзей пользователя id " + id);
        userService.removeFriends(id, friendId);
    }

    //вернуть список друзей пользователя с id
    @GetMapping("/{id}/friends")
    public List<User> userFriend(@PathVariable long id) {
        log.info("Запрос: вернуть список друзей пользователя с id " + id);
        return userService.userfriends(id);
    }

    //вернуть список общих друзей двух пользователей
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> commonFriend(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос: вернуть список общих друзей двух пользователей");
        return userService.commonFriends(id, otherId);
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}


