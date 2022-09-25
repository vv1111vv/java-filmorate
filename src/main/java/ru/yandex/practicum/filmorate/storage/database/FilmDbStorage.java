package ru.yandex.practicum.filmorate.storage.database;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Data
public class FilmDbStorage implements FilmStorage {
    private static final String INSERT_INTO_FILMS =
            "INSERT INTO FILMS (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_INTO_FILM_GENRES =
            "INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String DELETE_FROM_FILM_GENRES =
            "DELETE FROM FILM_GENRES WHERE film_id = ?";
    private static final String DELETE_FILM =
            "DELETE FROM FILMS WHERE film_id = ?";


    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film createFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(INSERT_INTO_FILMS, new String[]{"film_id"});
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setLong(5, film.getMpa().getId());
            return preparedStatement;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());

        final Set<Genre> filmGenres = film.getGenres();

        if (filmGenres != null) {
            filmGenres.forEach(x -> jdbcTemplate.update(INSERT_INTO_FILM_GENRES, film.getId(), x.getId()));
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info(String.valueOf(film));
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId(), film.getId());

        jdbcTemplate.update(DELETE_FROM_FILM_GENRES, film.getId());

        final Set<Genre> filmGenres = film.getGenres();

        if (filmGenres != null) {
            filmGenres.forEach(x -> jdbcTemplate.update(INSERT_INTO_FILM_GENRES, film.getId(), x.getId()));
        }
        return film;
    }
    private static final String GET_BY_ID =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, r.title " +
                    "FROM FILMS f JOIN MPA r ON f.mpa_id = r.mpa_id " +
                    "WHERE f.film_id = ?";

    @Override
    public Film getById(Long id) {

        List<Film> films = jdbcTemplate.query(GET_BY_ID, this::mapToFilm, id);
        return films.size() > 0 ? films.get(0) : null;
    }


    private Film mapToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new MPARating(rs.getInt("mpa_id"), rs.getString("title")));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbcTemplate.query("SELECT * FROM FILMS", this::mapToFilm);
    }

    public void deleteFilm(Film film) {
        jdbcTemplate.update(DELETE_FILM, film.getId());
    }
}
