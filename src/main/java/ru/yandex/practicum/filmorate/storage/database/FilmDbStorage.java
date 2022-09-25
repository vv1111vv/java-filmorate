package ru.yandex.practicum.filmorate.storage.database;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

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
            "INSERT INTO FILMS (film_id, name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_INTO_FILM_GENRES =
            "INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String DELETE_FROM_FILM_GENRES =
            "DELETE FROM FILM_GENRES WHERE film_id = ?";
//    private static final String GET_BY_ID =
//            "SELECT * FROM FILMS LEFT JOIN MPA ON FILMS.mpa_id = MPA.mpa_id WHERE film_id = ?";
    private static final String GET_GENRES_BY_FILM_ID =
            "SELECT * FROM FILM_GENRES INNER JOIN GENRES ON GENRES.genre_id = FILM_GENRES.genre_id WHERE film_id = ?";
    private static final String DELETE_FILM =
            "DELETE FROM FILMS WHERE film_id = ?";


    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film createFilm(Film film) {
        //log.info(String.valueOf(film));
        if (film.getId() == null) {
            film.setId(IdGenerator.nextFilmId());
        }

        jdbcTemplate.update(INSERT_INTO_FILMS, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId());

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

        List<Film> films = jdbcTemplate.query(GET_BY_ID, (rs, numRow) -> mapRowToFilm(rs, getFilmGenresById(id)), id);
        return films.size() > 0 ? films.get(0) : null;
    }
//public Film getById(Long id) throws NotFoundObjectException {
//    String sql =
//            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, r.title " +
//                    "FROM FILMS f JOIN MPA r ON f.mpa_id = r.mpa_id " +
//                    "WHERE f.film_id = ?";
//    List<Film> result = jdbcTemplate.query(sql, this::mapToFilm, id);
//    return result.size() > 0 ? result.get(0) : null;
//}


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
        List<Film> films = new ArrayList<>();

        List<Long> filmsId =
                jdbcTemplate.query("SELECT film_id FROM FILMS", ((rs, rowNum) -> rs.getLong("film_id")));

        for (Long filmId : filmsId) {
            films.add(getById(filmId));
        }

        return films;
    }

    private Set<Genre> getFilmGenresById(Long id) {

        return new HashSet<>(jdbcTemplate.query(GET_GENRES_BY_FILM_ID, (rs, getNum) -> Genre.builder().id(rs.getInt("genre_id"))
                .title(rs.getString("title")).build(), id));
    }

    public void deleteFilm(Film film) {
        jdbcTemplate.update(DELETE_FILM, film.getId());
    }

    private Film mapRowToFilm(ResultSet rs, Set<Genre> genres) throws SQLException {
        return new Film(rs.getLong("film_id"), rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new MPARating(rs.getInt("mpa_id"), rs.getString("title")),
                genres != null && genres.isEmpty() ? null : genres);
    }
}
