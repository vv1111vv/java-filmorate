package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.AbstractMPARatingStorage;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MPARatingStorage extends DataStorage<MPARating> implements AbstractMPARatingStorage {
    public MPARatingStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    @Transactional
    public Optional<MPARating> create(MPARating data) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement statement = connection.prepareStatement(getSqlCreate(),
                            new String[]{"id"});
                    statement.setString(1, data.getName());
                    return statement;
                }, keyHolder);

        return findById(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    @Transactional
    public Optional<MPARating> update(MPARating data) {
        return jdbcTemplate.update(getSqlUpdate(), data.getName(), data.getId()) == 0 ?
                Optional.empty() :
                Optional.of(data);
    }

    @Override
    protected MPARating mapRowToObject(ResultSet resultSet, int row) throws SQLException {
        return MPARating.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    protected String getSqlFindAll() {
        return "select id, name from mpa_ratings";
    }

    @Override
    protected String getSqlFindById() {
        return "select id, name from mpa_ratings where id = ?";
    }

    @Override
    protected String getSqlCreate() {
        return "insert into mpa_ratings (name) values (?)";
    }

    @Override
    protected String getSqlUpdate() {
        return "update mpa_ratings set name = ? where id = ?";
    }
}
