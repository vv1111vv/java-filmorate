package ru.yandex.practicum.filmorate.storage.database;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {
    List<Long> getFriendsIds(Long Userid);

    List<User> getAll();

    void create(Friendship friendship);

    void delete(Friendship friendship);
}
