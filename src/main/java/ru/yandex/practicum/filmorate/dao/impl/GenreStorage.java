package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.AbstractGenreStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class GenreStorage extends DataStorage<Genre> implements AbstractGenreStorage {
    public GenreStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    @Transactional
    public Optional<Genre> create(Genre data) {
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
    public Optional<Genre> update(Genre data) {
        return jdbcTemplate.update(getSqlUpdate(), data.getName(), data.getId()) == 0 ?
                Optional.empty() :
                Optional.of(data);
    }

    @Override
    public List<Genre> findByIds(List<Integer> ids) {
        return jdbcTemplate.query(getSqlFindByIds(ids.size()), this::mapRowToObject, ids.toArray());
    }

    @Override
    public List<Genre> findByFilm(int id) {
        return jdbcTemplate.query(getSqlFindByFilm(), this::mapRowToObject, id);
    }

    @Override
    public void removeByFilm(int id) {
        jdbcTemplate.update(getSqlRemoveByFilm(), id);
    }

    @Override
    protected Genre mapRowToObject(ResultSet resultSet, int row) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    protected String getSqlFindAll() {
        return "select id, name from genres";
    }

    @Override
    protected String getSqlFindById() {
        return "select id, name from genres where id = ?";
    }

    @Override
    protected String getSqlCreate() {
        return "insert into genres (name) values (?)";
    }

    @Override
    protected String getSqlUpdate() {
        return "update genres set name = ? where id = ?";
    }

    private String getSqlFindByIds(int idsLength) {
        String sqlCondition = String.join(" ,", Collections.nCopies(idsLength, "?"));
        return String.format("select g.id, g.name\n" +
                "from genres g\n" +
                "where g.id in (%s)", sqlCondition);
    }

    private String getSqlFindByFilm() {
        return "select g.id, g.name\n" +
                "from genres g\n" +
                "where id in (select fg.genre_id\n" +
                "             from films_genres fg\n" +
                "             where fg.film_id = ?)";
    }

    private String getSqlRemoveByFilm() {
        return "delete\n" +
                "from films_genres fg\n" +
                "where film_id = ?";
    }
}
