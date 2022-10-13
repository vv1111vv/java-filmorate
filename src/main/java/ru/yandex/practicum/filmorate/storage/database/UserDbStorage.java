package ru.yandex.practicum.filmorate.storage.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component("userDbStorage")
@Primary
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;


    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        final String sqlQuery = "select * from USERS";
        final List<User> users = jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser);
        if (users.size() == 0) {
            return Collections.emptyList();
        }
        return users;
    }

    @Override
    public User findById(long id) throws ObjectNotFoundException {
        final String sqlQuery = "select * from USERS where USER_ID = ?";
        final List<User> users = jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser, id);
        if (users.size() == 0) {
            log.debug(String.format("Пользователь %d не найден.", id));
            throw new ObjectNotFoundException("Пользователь не найден!");
        }
        return users.get(0);
    }

    public static User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getInt("USER_ID"),
                rs.getString("EMAIL"),
                rs.getString("LOGIN"),
                rs.getString("USER_NAME"),
                rs.getDate("BIRTHDAY").toLocalDate());
    }

    @Override
    public User create(User user) {
        String sqlQuery = "insert into USERS(EMAIL, LOGIN, USER_NAME, BIRTHDAY) " +
                "values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"USER_ID"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            final LocalDate birthday = user.getBirthday();
            if (birthday == null) {
                stmt.setNull(4, Types.DATE);
            } else {
                stmt.setDate(4, Date.valueOf(birthday));
            }
            return stmt;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User put(User user) throws ObjectNotFoundException {
        String sqlQuery = "update USERS set " +
                "EMAIL = ?, LOGIN = ?, USER_NAME = ?, BIRTHDAY = ? " +
                "where USER_ID = ?";
        int row = jdbcTemplate.update(sqlQuery
                , user.getEmail()
                , user.getLogin()
                , user.getName()
                , user.getBirthday()
                , user.getId());
        if (row == 0) {
            log.debug(String.format("Пользователь %d не найден.", user.getId()));
            throw new ObjectNotFoundException("Пользователь не найден!");
        }
        return user;
    }

    @Override
    public void deleteAll() {
        String sqlQuery = "delete from USERS";
        jdbcTemplate.update(sqlQuery);
    }

    public void delete(long id) throws ObjectNotFoundException {
        String sqlQuery = "delete from USERS where USER_ID = ?";
        if (jdbcTemplate.update(sqlQuery, id) == 0) {
            log.debug(String.format("Пользователь %d не найден.", id));
            throw new ObjectNotFoundException("Пользователь не найден!");
        }
        log.debug(String.format("Пользователь %d удалён из системы.", id));
    }

    public void addFriend(Long userId, Long friendId) {
        String sqlQuery = "insert into FRIENDS(USER_ID, FRIEND_ID, STATUS)" + "values (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendId, true);

        sqlQuery = "insert into FRIENDS(USER_ID, FRIEND_ID, STATUS)" + "values (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, friendId, userId, false);
    }

    @Override
    public boolean deleteFriend(Long userId, Long friendId) {
        String sqlQuery = "delete from FRIENDS where USER_ID = ? AND FRIEND_ID = ?";
        return (jdbcTemplate.update(sqlQuery, userId, friendId) > 0);
    }

    @Override
    public List<User> getFriends(Long userId) {
        final String sqlQuery = "select * from USERS where USER_ID in "
                + "(select FRIEND_ID from FRIENDS where USER_ID = ? AND STATUS = TRUE)";
        final List<User> users = jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser, userId);
        if (users.size() == 0) {
            return Collections.emptyList();
        }
        return users;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        final String sqlQuery = "select * from USERS where USER_ID in"
                + "(select * from (select FRIEND_ID from FRIENDS where USER_ID=? AND STATUS = TRUE)"
                + "where FRIEND_ID in (select FRIEND_ID from FRIENDS where USER_ID=? AND STATUS = TRUE))";
        final List<User> friends = jdbcTemplate.query(sqlQuery,
                UserDbStorage::makeUser,
                userId,
                otherId);
        if (friends.size() == 0) {
            return Collections.emptyList();
        }
        return friends;
    }

    @Override
    public List<Long> getUsersFilms(Long userId) {
        String sql = "SELECT film_id FROM LIKES WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("film_id"), userId);
    }


}