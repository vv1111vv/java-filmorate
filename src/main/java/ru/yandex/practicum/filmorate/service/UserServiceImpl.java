package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final String NO_DATA_FOUND = "Данные о пользователе не заполнены.";
    private static final String EMPTY_EMAIL = "Адрес электронной почты не может быть пустым.";
    private static final String INVALID_EMAIL = "Адрес электронной почты должен содержать символ \"@\".";
    private static final String EMPTY_LOGIN = "Логин не может быть пустым и содержать пробелы.";
    private static final String BIRTHDAY_IN_THE_FUTURE = "Дата рождения не может быть в будущем.";

    @Autowired
    public UserServiceImpl(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public List<User> findAll() {
        return userStorage.findAll();
    }

    @Override
    public User findById(long id) throws ObjectNotFoundException {
        return userStorage.findById(id);
    }

    @Override
    public User create(User user) throws ValidationException {
        String message = check(user);
        if (!message.isBlank()) {
            log.debug("Ошибка при попытке добавления пользователя: " + message);
            throw new ValidationException(message);
        }
        log.debug(String.format("Создан новый пользователь %d.", user.getId()));
        return userStorage.create(user);
    }

    @Override
    public User put(User user) throws ValidationException, ObjectNotFoundException {
        String message = check(user);
        if (!message.isBlank()) {
            log.debug("Ошибка при попытке редактирования пользователя: " + message);
            throw new ValidationException(message);
        }
        log.debug(String.format("Изменения для пользователя %d успешно приняты.", user.getId()));
        return userStorage.put(user);
    }

    @Override
    public void deleteAll() {
        log.debug("Все пользователи удалены из системы. :(");
        userStorage.deleteAll();
    }

    @Override
    public void delete(long id) throws ObjectNotFoundException {
        userStorage.delete(id);
    }

    @Override
    public User addFriend(Long userId, Long friendId) throws ObjectNotFoundException {
        User user = userStorage.findById(userId);
        User userFriend = userStorage.findById(friendId);
        if (user == null) {
            log.debug(String.format("Ошибка при попытке добавить в друзья. Пользователь с id %d не найден", userId));
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        if (userFriend == null) {
            log.debug(String.format("Ошибка при попытке добавить в друзья. Пользователь с id %d не найден", friendId));
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", friendId));
        }
        userStorage.addFriend(userId, friendId);
        log.debug(String.format("Пользователь %d добавил в друзья пользователя %d", userId, friendId));
        return user;
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) throws ObjectNotFoundException {
        User user = userStorage.findById(userId);
        User userFriend = userStorage.findById(friendId);
        if (user == null) {
            log.debug(String.format("Ошибка при попытке удалить из друзей. Пользователь с id %d не найден", userId));
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        if (userFriend == null) {
            log.debug(String.format("Ошибка при попытке удалить из друзей. Пользователь с id %d не найден", friendId));
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", friendId));
        }
        if (userStorage.deleteFriend(userId, friendId)) {
            log.debug(String.format("Пользователь %d удалил из друзей пользователя %d", userId, friendId));
            return user;
        } else {
            return null;
        }

    }

    @Override
    public List<User> getFriends(Long userId) throws ObjectNotFoundException {
        User user = userStorage.findById(userId);
        if (user == null) {
            log.debug(String.format("Ошибка при попытке вывести список друзей. Пользователь с id %d не найден", userId));
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        return userStorage.getFriends(userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) throws ObjectNotFoundException {
        User user = userStorage.findById(userId);
        User otherUser = userStorage.findById(otherId);
        if (user == null) {
            log.debug(String.format("Ошибка при попытке найти общих друзей. Пользователь с id %d не найден", userId));
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        if (otherUser == null) {
            log.debug(String.format("Ошибка при попытке найти общих друзей. Пользователь с id %d не найден", otherId));
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден", otherId));
        }
        return userStorage.getCommonFriends(userId,otherId);
    }


    //ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
    private String check(User user) throws ValidationException {

        String message = "";
        if (user == null) {
            message = NO_DATA_FOUND;
        } else if (user.getEmail() == null || user.getEmail().isBlank()) {
            message = EMPTY_EMAIL;
        } else if (!user.getEmail().contains("@")) {
            message = INVALID_EMAIL;
        } else if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            message = EMPTY_LOGIN;
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            message = BIRTHDAY_IN_THE_FUTURE;
        } else if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return message;
    }
}

