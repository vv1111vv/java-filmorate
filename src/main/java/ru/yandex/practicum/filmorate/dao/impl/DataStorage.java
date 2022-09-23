package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.AbstractDataStorage;
import ru.yandex.practicum.filmorate.model.AbstractData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class DataStorage<T extends AbstractData> implements AbstractDataStorage<T> {
    protected final JdbcTemplate jdbcTemplate;

    private String sqlFindAll;
    private String sqlFindById;
    private String sqlCreate;
    private String sqlUpdate;
    private String sqlDelete;

    @Autowired
    public DataStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<T> findAll() {
        return jdbcTemplate.query(getSqlFindAll(), this::mapRowToObject);
    }

    @Override
    public Optional<T> findById(int id) {
        List<T> results = jdbcTemplate.query(getSqlFindById(), this::mapRowToObject, id);
        return results.size() == 0 ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public abstract Optional<T> create(T data);

    @Override
    public abstract Optional<T> update(T data);

    protected abstract T mapRowToObject(ResultSet resultSet, int row) throws SQLException;
}
