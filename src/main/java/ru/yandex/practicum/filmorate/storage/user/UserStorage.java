package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.util.ArrayList;

/*
Интерфейс определяет методы:
- создать пользователя
- обновить пользователя
- получить список пользователей
 */

@Component
public interface UserStorage {
    //создать пользователя
    public User create(User user);

    //обновить пользователя
    public User update(User user);

    //получить список всех пользователей
    public ArrayList<User> getAllUsers();

}
