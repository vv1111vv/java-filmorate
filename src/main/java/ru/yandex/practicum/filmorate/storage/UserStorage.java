package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAll();
    User findById(long id) throws ObjectNotFoundException;
    User create(User user);
    User put(User user) throws  ObjectNotFoundException;
    void deleteAll();
    void delete(long userId) throws ObjectNotFoundException;
    void addFriend(Long userId, Long friendId);
    boolean deleteFriend(Long userId, Long friendId);
    List<User> getFriends(Long userId);
    List<User> getCommonFriends(Long userId, Long otherId);

    List<Long> getUsersFilms(Long userId);
}
