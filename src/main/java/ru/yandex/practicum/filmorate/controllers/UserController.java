package ru.yandex.practicum.filmorate.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FeedService feedService;

    @Autowired
    public UserController(UserService userService, FeedService feedService) {
        this.userService = userService;
        this.feedService = feedService;
    }

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable("id") long id) throws ObjectNotFoundException {
        return userService.findById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
        return userService.create(user);
    }

    @PutMapping
    public User put(@Valid @RequestBody User user) throws ValidationException, ObjectNotFoundException {
        return userService.put(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") long userId) throws ObjectNotFoundException {
        userService.delete(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id") long id, @PathVariable("friendId") long friendId) throws ObjectNotFoundException {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFromFriends(@PathVariable("id") long id, @PathVariable("friendId") long friendId) throws ObjectNotFoundException {
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable("id") long id) throws ObjectNotFoundException {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") long id, @PathVariable("otherId") long otherId) throws ObjectNotFoundException {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getFeed(@PathVariable("id") long id) throws ObjectNotFoundException {
        return feedService.getFeed(id);
    }

//    @GetMapping("/{id}/recommendations")
//    public List<Film> getRecommendationsByUser(@PathVariable long id
//            , @RequestParam(name = "count", defaultValue = "10") int count){
//        return userService.getRecommendationsByUser(id, count);
//    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable Integer id)
            throws UserNotFoundException {
        return userService.getRecommendations(id);
    }

    //метод для тестов
    public void deleteAll() {
        userService.deleteAll();
    }

}



