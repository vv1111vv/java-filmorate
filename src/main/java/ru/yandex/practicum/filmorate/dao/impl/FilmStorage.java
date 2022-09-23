package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.AbstractFilmStorage;
import ru.yandex.practicum.filmorate.dao.AbstractGenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmStorage extends DataStorage<Film> implements AbstractFilmStorage {
    private final AbstractGenreStorage genreStorage;

    public FilmStorage(JdbcTemplate jdbcTemplate, AbstractGenreStorage genreStorage) {
        super(jdbcTemplate);
        this.genreStorage = genreStorage;
    }

    @Override
    @Transactional
    public Optional<Film> create(Film data) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(getSqlCreate(), new String[]{"id"});
                statement.setString(1, data.getName());
                statement.setString(2, data.getDescription());
                statement.setDate(3, Date.valueOf(data.getReleaseDate()));
                statement.setInt(4, data.getDuration());
                statement.setInt(5, data.getMpa().getId());
                return statement;
            }, keyHolder);

            data.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
            return addGenresToFilm(data, keyHolder);

        } catch (Exception e) {
            throw new ValidationException("Получены некорректные данные", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Optional<Film> update(Film data) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(getSqlUpdate(), new String[]{"id"});
                statement.setString(1, data.getName());
                statement.setString(2, data.getDescription());
                statement.setDate(3, Date.valueOf(data.getReleaseDate()));
                statement.setInt(4, data.getDuration());
                statement.setInt(5, data.getMpa().getId());
                statement.setInt(6, data.getId());
                return statement;
            }, keyHolder);

            return addGenresToFilm(data, keyHolder);

        } catch (Exception e) {
            throw new NotFoundException("Получены некорректные данные", e);
        }

    }

    private Optional<Film> addGenresToFilm(Film data, KeyHolder keyHolder) {
        Optional<Film> optional = findById(Objects.requireNonNull(keyHolder.getKey()).intValue());
        optional.ifPresent(film -> {
            if (!film.getGenres().isEmpty()) {
                genreStorage.removeByFilm(data.getId());
            }
        });

        data.getGenres().forEach(genre -> {
            try {
                jdbcTemplate.update(getSqlAddGenreToFilm(), genre.getId(), data.getId());
            } catch (DuplicateKeyException e) {
                log.warn(e.getMessage());
            }
        });


        if (data.getGenres() != null && data.getGenres().size() != 0) {
            optional.ifPresent(film -> {
                film.setGenres(genreStorage.findByIds(data.getGenres().stream().mapToInt(Genre::getId).boxed().collect(Collectors.toList())));
            });
        } else {
            optional.ifPresent(film -> film.setGenres(new ArrayList<>()));
        }
        return optional;
    }

    @Override
    protected Film mapRowToObject(ResultSet resultSet, int row) throws SQLException {
        return Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("film_name"))
                .description(resultSet.getString("film_description"))
                .releaseDate(LocalDate.parse(resultSet.getString("film_release_date"), DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .duration(resultSet.getInt("film_duration"))
                .mpa(MPARating.builder()
                        .id(resultSet.getInt("rating_id"))
                        .name(resultSet.getString("rating_name")).build())
                .genres(genreStorage.findByFilm(resultSet.getInt("id"))).build();
    }

    @Override
    public void addLike(int id, int userId) {
        jdbcTemplate.update(getSqlAddLike(), id, userId);
    }

    @Override
    public boolean removeLike(int id, int userId) {
        return jdbcTemplate.update(getSqlRemoveLike(), id, userId) == 1;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return jdbcTemplate.query(getSqlPopularFilms(), this::mapRowToObject, count);
    }

    @Override
    protected String getSqlFindAll() {
        return "select f.id            as film_id,\n" +
                "       f.name         as film_name,\n" +
                "       f.description  as film_description,\n" +
                "       f.release_date as film_release_date,\n" +
                "       f.duration     as film_duration,\n" +
                "       mr.id          as rating_id,\n" +
                "       mr.name        as rating_name\n" +
                "from films f\n" +
                "         join mpa_ratings mr on mr.id = f.mpa";
    }

    @Override
    protected String getSqlFindById() {
        return "select f.id            as film_id,\n" +
                "       f.name         as film_name,\n" +
                "       f.description  as film_description,\n" +
                "       f.release_date as film_release_date,\n" +
                "       f.duration     as film_duration,\n" +
                "       mr.id          as rating_id,\n" +
                "       mr.name        as rating_name\n" +
                "from films f\n" +
                "         join mpa_ratings mr on mr.id = f.mpa\n" +
                "where f.id = ?";
    }

    @Override
    protected String getSqlCreate() {
        return "insert into films (name, description, release_date, duration, mpa) values (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getSqlUpdate() {
        return "update films" +
                "    set name = ?," +
                "    description = ?," +
                "    release_date = ?," +
                "    duration = ?," +
                "    mpa = ?" +
                "where id = ?";
    }

    private String getSqlAddLike() {
        return "insert into likes (film_id, user_id)\n" +
                "values (?, ?);";
    }

    private String getSqlRemoveLike() {
        return "delete\n" +
                "from likes l\n" +
                "where l.film_id = ?\n" +
                "  and l.user_id = ?";
    }

    private String getSqlPopularFilms() {
        return "select f.id            as film_id,\n" +
                "       f.name         as film_name,\n" +
                "       f.description  as film_description,\n" +
                "       f.release_date as film_release_date,\n" +
                "       f.duration     as film_duration,\n" +
                "       mr.id          as rating_id,\n" +
                "       mr.name        as rating_name\n" +
                "from films f\n" +
                "         join mpa_ratings mr on mr.id = f.mpa\n" +
                "where f.id in (select f.id\n" +
                "               from films f\n" +
                "                        left join likes l on f.id = l.film_id\n" +
                "               group by f.id\n" +
                "               order by count(l.user_id) desc\n" +
                "               limit ?)";
    }

    private String getSqlAddGenreToFilm() {
        return "insert into films_genres (genre_id, film_id)\n" +
                "values (?, ?)";
    }
}
