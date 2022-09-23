package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface AbstractUserStorage extends AbstractDataStorage<User> {
    List<User> getFriends(int id);

    void addFriend(int id, int friendId);

    List<User> getCommonFriends(int id, int otherId);

    void removeFriend(int id, int friendId);
}
