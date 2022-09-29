package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
//import org.apache.catalina.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private static final String SELECT = "SELECT friend_id FROM FRIENDSHIPS WHERE user_id = ?";
//    private static final String SELECT = "SELECT * from users, friendships where users.user_id = friendships.friend_id AND friendships.user_id = ?";
    private static final String INSERT = "INSERT INTO FRIENDSHIPS (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE = "DELETE FROM FRIENDSHIPS WHERE user_id = ? AND friend_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> getFriendsIds(Long Userid) {
        return jdbcTemplate.query(SELECT, (rs, numRow) -> rs.getLong("friend_id"), Userid);
    }
    @Override
    public List<User> getAll() {
        String sql = "SELECT * from users, friendships where users.user_id = friendships.friend_id AND friendships.user_id = ?";
        return jdbcTemplate.query(sql, this::mapToUser);
    }

    private User mapToUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());
        return user;
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
