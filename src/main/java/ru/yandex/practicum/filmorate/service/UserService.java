package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistByIdException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.database.UserDbStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final FriendshipStorage friendshipStorage;
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserDbStorage userStorage, FriendshipStorage friends) {
        this.friendshipStorage = friends;
        this.userStorage = userStorage;
    }

    public User getById(Long id) throws UserDoesNotExistByIdException {
        return userStorage.getById(id);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        log.info("создан пользователь {}", user.getName());
        return userStorage.createUser(user);
    }

    public User update(User user) {
        log.info("обновлен пользователь {}", user.getName());
        return userStorage.update(user);
    }

    public void addFriend(Long id, Long friendId) throws UserDoesNotExistByIdException {
        if (id < 1 || friendId < 1) {
            log.info("ошибка из-за неверного id");
            throw new UserDoesNotExistByIdException("пользователь не может существовать с таким айди");
        }
        friendshipStorage.create(Friendship
                .builder()
                .user(getById(id))
                .friend(getById(friendId))
                .build());
    }


    public void deleteFriend(Long id, Long friendId) {
        if (id < 1 || friendId < 1) {
            log.info("ошибка из-за неверного id");
            throw new UserDoesNotExistByIdException("пользователь не может существовать с таким айди");
        }
        friendshipStorage.delete(Friendship
                .builder()
                .user(getById(id))
                .friend(getById(friendId))
                .build());
    }

    public List<User> getFriendsOf(Long id) {
        if (id < 1) {
            log.info("ошибка из-за неверного id");
            throw new UserDoesNotExistByIdException("пользователь не может существовать с таким айди");
        }
        return friendshipStorage.getFriendsIds(getById(id).getId())
                .stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }


    public List<User> getCommonFriends(Long id, Long otherId) throws UserDoesNotExistByIdException {
        Set<Long> common = new HashSet<>(friendshipStorage.getFriendsIds(getById(id).getId()));
        common.retainAll(friendshipStorage.getFriendsIds(otherId));

        return common
                .stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }
}

