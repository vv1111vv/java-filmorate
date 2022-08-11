package ru.yandex.practicum.filmorate.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;



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
}

