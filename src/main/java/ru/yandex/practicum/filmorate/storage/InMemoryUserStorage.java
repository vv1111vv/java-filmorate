package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.controllers.UserController;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private long lastUsedId = 0;
    private final HashMap<Long, User> users = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(long id) throws ObjectNotFoundException {

        if (!users.containsKey(id)) {
            throw new ObjectNotFoundException("Пользователь не найден.");
        }
        return users.get(id);
    }

    @Override
    public User create(@Valid @RequestBody User user)  {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Сохранён пользователь: {}", user.toString());
        return user;
    }

    @Override
    public User put(User user) throws ObjectNotFoundException {
        if (!users.containsKey(user.getId())) {
            throw new ObjectNotFoundException("Пользователь не найден.");
        }
        users.put(user.getId(), user);
        log.debug("Обновлён пользователь: {}", user.toString());
        return user;
    }

    @Override
    public void deleteAll() {
        users.clear();
    }

    public void delete(long id) {
        log.debug("Удалён пользователь: {}", id);
        users.remove(id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
    }

    @Override
    public boolean deleteFriend(Long userId, Long friendId) {
        return false;
    }

    @Override
    public List<User> getFriends(Long userId) {
        return null;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return null;
    }

    private long getNextId() {
        return ++lastUsedId;
    }

}
