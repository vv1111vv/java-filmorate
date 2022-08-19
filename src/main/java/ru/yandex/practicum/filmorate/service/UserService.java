package ru.yandex.practicum.filmorate.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
добавление в друзья, удаление из друзей, вывод списка общих друзей
 */

@Service
@Slf4j
@NoArgsConstructor
public class UserService {
    private UserStorage userStorage;
    private long idgenerator;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        ++idgenerator;
        user.setId(idgenerator);
        return userStorage.create(user);
    }

    //обновить пользователя;
    public User update(User user) {

        return userStorage.update(user);
    }

    //получить список всех пользователей
    public ArrayList<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    //получить пользователя по id
    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    //добавить пользователя в друзья
    public void addFriends(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friend.getId());
        friend.getFriends().add(userId);
    }

    //удалить пользователя из друзей
    public void removeFriends(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friend.getId());
        friend.getFriends().add(userId);
    }

    //получить список друзей пользователя
    public List<User> userfriends(long id) {
        User user = getUserById(id);
//        List<User> userFriends = new ArrayList<>();
//        user.getFriends().stream().forEach(e -> userFriends.add(getUserById(e)));
//        return userFriends;
        return user.getFriends().stream().map(userStorage::getUserById).collect(Collectors.toList());
    }

    //получить список общих друзей пользователя
    public List<User> commonFriends(long idUser, long idOther) {
        User user = userStorage.getUserById(idUser);
        User otherUser = userStorage.getUserById(idOther);
        Set<Long> userFriends = user.getFriends();
        Set<Long> otherFriends = otherUser.getFriends();
//        List<User> commonUserFriends = new ArrayList<>();
//        userFriends.stream()
//                .filter(otherFriends::contains)
//                .forEach(e -> commonUserFriends.add(getUserById(e)));
//        return commonUserFriends;
        return userFriends.stream()
                .filter(otherFriends::contains).map(userStorage::getUserById).collect(Collectors.toList());
    }
}

