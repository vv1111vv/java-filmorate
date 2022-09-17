package ru.yandex.practicum.filmorate.controllers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistByIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@Data
public class UserController {
    private final UserService service;

    @GetMapping("/users")
    public List<User> getUsers() {
        return service.getUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) throws UserDoesNotExistByIdException {
        return id < 1 ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody User user) {
        return service.createUser(user);
    }

    @PutMapping("/users")
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        return user.getId() < 1 ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(service.update(user), HttpStatus.OK);
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        service.addFriend(id, friendId);
    }


    @DeleteMapping("/users/{id}/friends/{friendId}")
    public String deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        service.deleteFriend(id, friendId);
        return "друг удален";
    }

    @GetMapping("/users/{id}/friends")
    public List<User> getFriendsOf(@PathVariable Long id) {
        return service.getFriendsOf(id);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) throws UserDoesNotExistByIdException {
        return service.getCommonFriends(id, otherId);
    }
}



