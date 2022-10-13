package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(long id) throws ObjectNotFoundException;
    User create(User user) throws ValidationException;
    User put(User user) throws ValidationException, ObjectNotFoundException;
    void deleteAll();
    void delete(long userId) throws  ObjectNotFoundException;
    User addFriend(Long userId, Long friendId) throws ObjectNotFoundException;
    User deleteFriend(Long userId, Long friendId) throws ObjectNotFoundException;
    List<User> getFriends(Long userId) throws ObjectNotFoundException;
    List<User> getCommonFriends(Long userId, Long otherId) throws ObjectNotFoundException;
    Collection<Film> getRecommendations(long userId) throws UserNotFoundException;
}
