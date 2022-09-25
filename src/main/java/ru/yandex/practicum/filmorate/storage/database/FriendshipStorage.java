package ru.yandex.practicum.filmorate.storage.database;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {

//    Collection<Long> getFriendsIds(Long id);

    List<Long> getFriendsIds(Long Userid);

    //    @Override
    //    public List<User> getFriendsIds(Long Userid) {
    //        return jdbcTemplate.query(SELECT, (rs, numRow) -> rs.getLong("friend_id"), Userid);
    //    }
    List<User> getAll();

    void create(Friendship friendship);

    void delete(Friendship friendship);
}
