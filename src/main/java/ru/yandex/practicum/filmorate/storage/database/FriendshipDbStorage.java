package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private static final String SELECT = "SELECT friend_id FROM FRIENDSHIPS WHERE user_id = ?";
    private static final String INSERT = "INSERT INTO FRIENDSHIPS (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE = "DELETE FROM FRIENDSHIPS WHERE user_id = ? AND friend_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Long> getFriendsIds(Long id) {
        return jdbcTemplate.query(SELECT, (rs, numRow) -> rs.getLong("friend_id"), id);
    }

    @Override
    public void create(Friendship friendship) {
        jdbcTemplate.update(INSERT, friendship.getUser().getId(), friendship.getFriend().getId());
    }

    @Override
    public void delete(Friendship friendship) {
        jdbcTemplate.update(DELETE, friendship.getUser().getId(), friendship.getFriend().getId());
    }
}
