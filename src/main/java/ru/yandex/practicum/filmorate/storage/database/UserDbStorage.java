package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserDoesNotExistByIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String GET_ALL_USERS = "SELECT * FROM USERS";
    private static final String GET_USER_BY_ID = "SELECT * FROM USERS WHERE user_id = ?";
    private static final String CREATE_USER =
            "INSERT INTO USERS (login, name, email, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE USERS SET login = ?, name = ?, email = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE_USER = "DELETE FROM USERS WHERE user_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query(GET_ALL_USERS, (rs, rowNum) -> mapRowToUser(rs));
    }

    @Override
    public User getById(Long id) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(GET_USER_BY_ID, id);

        if (rowSet.next()) {
            User user = new User(rowSet.getString("login"),
                    rowSet.getString("name"),
                    rowSet.getString("email"),
                    Objects.requireNonNull(rowSet.getDate("birthday")).toLocalDate());
            user.setId(id);

            return user;
        } else {
            throw new UserDoesNotExistByIdException("Пользователь не найден");
        }
    }


    @Override
    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(CREATE_USER, new String[]{"user_id"});
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));
            return preparedStatement;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(UPDATE_USER, user.getLogin(), user.getName(), user.getEmail(), user.getBirthday(), user.getId());
        return user;
    }


    public void delete(User user) {
        jdbcTemplate.update(DELETE_USER, user.getId());
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(rs.getLong("user_id"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getDate("birthday").toLocalDate());
    }
}