package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.AbstractUserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserStorage extends DataStorage<User> implements AbstractUserStorage {
    public UserStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    @Transactional
    public Optional<User> create(User data) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(getSqlCreate(), new String[]{"id"});
            statement.setString(1, data.getLogin());
            statement.setString(2, data.getName());
            statement.setString(3, data.getEmail());
            statement.setDate(4, Date.valueOf(data.getBirthday()));
            return statement;
        }, keyHolder);

        return findById(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    @Transactional
    public Optional<User> update(User data) {
        return jdbcTemplate.update(getSqlUpdate(),
                data.getLogin(),
                data.getName(),
                data.getEmail(),
                data.getBirthday(),
                data.getId()) == 0 ?
                Optional.empty() :
                Optional.of(data);
    }

    @Override
    protected User mapRowToObject(ResultSet resultSet, int row) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("id"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .email(resultSet.getString("email"))
                .birthday(LocalDate.parse(resultSet.getString("birthday"), DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }

    @Override
    public List<User> getFriends(int id) {
        return jdbcTemplate.query(getSqlFriends(), this::mapRowToObject, id);
    }

    public void addFriend(int id, int friendId) {
        jdbcTemplate.update(getSqlAddFriend(), id, friendId);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        return jdbcTemplate.query(getSqlCommonFriends(), this::mapRowToObject, id, otherId);
    }

    public void removeFriend(int id, int friendId) {
        jdbcTemplate.update(getSqlRemoveFriend(), id, friendId);
    }

    @Override
    protected String getSqlFindAll() {
        return "select id, login, name, email, birthday from users";
    }

    @Override
    protected String getSqlFindById() {
        return "select id, login, name, email, birthday from users where id = ?";
    }

    @Override
    protected String getSqlCreate() {
        return "insert into users (login, name, email, birthday) values (?, ?, ?, ?)";
    }

    @Override
    protected String getSqlUpdate() {
        return "update users set login = ?, name = ?, email = ?, birthday = ? where id = ?";
    }

    private String getSqlFriends() {
        return "select u.id, u.login, u.name, u.email, u.birthday\n" +
                "from users u\n" +
                "where u.id in (select f.friend_id\n" +
                "               from friends f\n" +
                "               where f.user_id = ?)";
    }

    private String getSqlAddFriend() {
        return "insert into friends (user_id, friend_id) values (?, ?)";
    }

    private String getSqlCommonFriends() {
        return "select u.id, u.login, u.name, u.email, u.birthday\n" +
                "from users u\n" +
                "where u.id in (select f.friend_id\n" +
                "               from friends f\n" +
                "               where f.user_id = ?\n" +
                "               intersect\n" +
                "               select f.friend_id\n" +
                "               from friends f\n" +
                "               where f.user_id = ?)";
    }

    private String getSqlRemoveFriend() {
        return "delete\n" +
                "from friends f\n" +
                "where f.user_id = ?\n" +
                "  and f.friend_id = ?";
    }
}
