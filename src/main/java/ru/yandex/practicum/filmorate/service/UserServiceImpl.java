package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;
    private static final String NO_DATA_FOUND = "Данные о пользователе не заполнены.";
    private static final String EMPTY_EMAIL = "Адрес электронной почты не может быть пустым.";
    private static final String INVALID_EMAIL = "Адрес электронной почты должен содержать символ \"@\".";
    private static final String EMPTY_LOGIN = "Логин не может быть пустым и содержать пробелы.";
    private static final String BIRTHDAY_IN_THE_FUTURE = "Дата рождения не может быть в будущем.";

    public UserServiceImpl(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserStorage userStorage, FeedStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.feedStorage = feedStorage;
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
        feedStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType("FRIEND")
                .operation("ADD")
                .timestamp(new Timestamp(System.currentTimeMillis()).getTime())
                .entityId(friendId)
                .build());
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
            feedStorage.addEvent(Event.builder()
                    .userId(userId)
                    .eventType("FRIEND")
                    .operation("REMOVE")
                    .timestamp(new Timestamp(System.currentTimeMillis()).getTime())
                    .entityId(friendId)
                    .build());
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

    public Collection<Film> getRecommendations(long userId) throws UserNotFoundException {

        Map<Long, List<Long>> filmsOfUsers = new HashMap<>();

        List<User> users = userStorage.findAll();

        for (User user : users) {
            filmsOfUsers.put(user.getId(), userStorage.getUsersFilms(user.getId()));
        }

        long maxMatches = 0;
        Set<Long> similarity = new HashSet<>();
        for (Long id : filmsOfUsers.keySet()) {
            if (id == userId) continue;

            long numberOfMatches = filmsOfUsers.get(id).stream()
                    .filter(filmId -> filmsOfUsers.get(userId).contains(filmId)).count();

            if (numberOfMatches == maxMatches & numberOfMatches != 0) {
                similarity.add(id);
            }

            if (numberOfMatches > maxMatches) {
                maxMatches = numberOfMatches;
                similarity = new HashSet<>();
                similarity.add(id);
            }
        }

        if (maxMatches == 0) return new HashSet<>();
        else return similarity.stream().flatMap(idUser -> userStorage.getUsersFilms(idUser).stream())
                .filter(filmId -> !filmsOfUsers.get(userId).contains(filmId))
                .map(filmId -> filmStorage.findById(filmId))
                .collect(Collectors.toSet());
    }

}