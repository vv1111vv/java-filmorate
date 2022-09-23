package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.AbstractDataStorage;
import ru.yandex.practicum.filmorate.dao.AbstractUserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService extends AbstractDataService<User> {
    private final AbstractUserStorage userStorage;

    public UserService(AbstractDataStorage<User> storage, AbstractUserStorage userStorage) {
        super(storage);
        this.userStorage = userStorage;
    }

    @Override
    public Optional<User> create(User data) {
        if (data.getName() == null || data.getName().isBlank()) {
            data.setName(data.getLogin());
        }

        return super.create(data);
    }

    public List<User> getFriends(int id) {
        try {
            return userStorage.getFriends(id);
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new NotFoundException(String.format("Ошибка получения друзей для пользователя, id=%d", id));
        }
    }

    public void addFriend(int id, int friendId) {
        try {
            userStorage.addFriend(id, friendId);
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new NotFoundException(String.format("Ошибка добавления друга, id=%d, friendId=%d", id, friendId));
        }
    }

    public List<User> getCommonFriends(int id, int otherId) {
        try {
            return userStorage.getCommonFriends(id, otherId);
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new NotFoundException(String.format("Ошибка получения общих друзей, id=%d, friendId=%d", id, otherId));
        }
    }

    public void removeFriend(int id, int friendId) {
        try {
            userStorage.removeFriend(id, friendId);
        } catch (DataIntegrityViolationException e) {
            log.warn(e.getMessage());
            throw new NotFoundException(String.format("Ошибка получения общих друзей, id=%d, friendId=%d", id, friendId));
        }
    }
}
