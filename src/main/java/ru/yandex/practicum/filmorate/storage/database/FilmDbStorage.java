package ru.yandex.practicum.filmorate.storage.database;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@Primary
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAll() {
        final String sqlQuery = "select * from FILMS left join MPA M on FILMS.MPA_ID = M.MPA_ID";
        final List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm);
        if (films.size() == 0) {
            return Collections.emptyList();
        }
        for (Film film : films) {
            setGenre(film);
        }
        return films;
    }

    @Override
    public Film findById(long id) throws ObjectNotFoundException {
        final String sqlQuery = "select * from FILMS left join MPA M on FILMS.MPA_ID = M.MPA_ID where FILM_ID = ?";
        final List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, id);
        if (films.size() == 0) {
            log.debug(String.format("Фильм %d не найден.", id));
            throw new ObjectNotFoundException("Фильм не найден!");
        }
        Film film = films.get(0);
        setGenre(film);
        return film;
    }

    @Override
    public Film create(Film film) {
        final String sqlQuery = "insert into FILMS(FILM_NAME, DESCRIPTION, DURATION, MPA_ID, RELEASE_DATE) " +
                "values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"FILM_ID"});
            stmt.setString(1, film.getName());

            final String description = film.getDescription();
            if (description.isBlank()) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, description);
            }

            stmt.setInt(3, film.getDuration());

            final int mpa = film.getMpa().getId();
            if (mpa == 0) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, mpa);
            }

            final LocalDate releaseDate = film.getReleaseDate();
            if (releaseDate == null) {
                stmt.setNull(5, Types.DATE);
            } else {
                stmt.setDate(5, Date.valueOf(releaseDate));
            }
            return stmt;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());

        //Обновить таблицу с жанрами
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                String sqlQueryGenre = "insert into FILM_GENRES(FILM_ID, GENRE_ID) " + "values (?, ?)";
                jdbcTemplate.update(sqlQueryGenre, film.getId(), g.getId());
            }
        }

        return film;
    }

    @Override
    public Film put(Film film) throws ObjectNotFoundException {
        String sqlQuery = "update FILMS set " +
                "FILM_NAME = ?, DESCRIPTION = ?, DURATION = ?, MPA_ID = ? , RELEASE_DATE = ?" +
                "where FILM_ID = ?";
        int row = jdbcTemplate.update(sqlQuery
                , film.getName()
                , film.getDescription()
                , film.getDuration()
                , film.getMpa().getId()
                , film.getReleaseDate()
                , film.getId());
        if (row == 0) {
            log.debug(String.format("Фильм %d не найден.", film.getId()));
            throw new ObjectNotFoundException("Фильм не найден");
        }
        //Обновить таблицу с жанрами
        sqlQuery = "delete from FILM_GENRES where FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                sqlQuery = "merge into FILM_GENRES(FILM_ID, GENRE_ID) " + "values (?, ?)";
                jdbcTemplate.update(sqlQuery, film.getId(), g.getId());
            }
            setGenre(film);
        }

        return film;
    }

    @Override
    public void deleteAll() {
        String sqlQuery = "delete from FILMS";
        jdbcTemplate.update(sqlQuery);
    }

    @Override
    public void delete(long id) throws ObjectNotFoundException {
        String sqlQuery = "delete from FILMS where FILM_ID = ?";
        if (jdbcTemplate.update(sqlQuery, id) == 0) {
            log.debug(String.format("Фильм %d не найден.", id));
            throw new ObjectNotFoundException("Фильм не найден.");
        }
        log.debug(String.format("Фильм %d удалён из системы.", id));
    }

    @Override
    public boolean addLike(long filmId, long userId) {
        final String sqlQuery = "insert into LIKES(USER_ID, FILM_ID) " + "values (?, ?)";
        return (jdbcTemplate.update(sqlQuery, userId, filmId) > 0);
    }

    @Override
    public boolean deleteLike(long filmId, long userId) {
        String sqlQuery = "delete from LIKES where USER_ID = ? AND FILM_ID = ?";
        return (jdbcTemplate.update(sqlQuery, userId, filmId) > 0);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        final String sqlQuery = "select F.*, M.MPA_NAME, COUNT(L.USER_ID)from FILMS F "
                + "left join LIKES L on F.film_id = L.film_id "
                + "left join MPA M on F.MPA_ID = M.MPA_ID "
                + "group by F.film_id, film_name, description, duration, f.mpa_id, m.mpa_id, m.mpa_name, release_date "
                + "order by COUNT(L.USER_ID) desc LIMIT ?";
        final List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, count);
        if (films.size() == 0) {
            return Collections.emptyList();
        }
        for (Film film : films) {
            setGenre(film);
        }
        return films;
    }

    @Override
    public MPARating findMpaById(long id) throws ObjectNotFoundException {
        final String sqlQuery = "select * from MPA where MPA_ID = ?";
        final List<MPARating> mpa = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeMpa, id);
        if (mpa.size() == 0) {
            log.debug(String.format("Неизвестный рейтинг %d.", id));
            throw new ObjectNotFoundException("Неизвестный рейтинг");
        }
        return mpa.get(0);
    }

    @Override
    public List<MPARating> findAllMpa() {
        final String sqlQuery = "select * from MPA";
        final List<MPARating> mpa = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeMpa);
        if (mpa.size() == 0) {
            return Collections.emptyList();
        }
        return mpa;
    }

    @Override
    public Genre findGenreById(long id) throws ObjectNotFoundException {
        final String sqlQuery = "select * from GENRES where GENRE_ID = ?";
        final List<Genre> genre = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeGenre, id);
        if (genre.size() == 0) {
            log.debug(String.format("Неизвестный жанр %d.", id));
            throw new ObjectNotFoundException("Неизвестный жанр");
        }
        return genre.get(0);
    }

    @Override
    public List<Genre> findAllGenre() {
        final String sqlQuery = "select * from GENRES";
        final List<Genre> genres = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeGenre);
        if (genres.size() == 0) {
            return Collections.emptyList();
        }
        return genres;
    }


    private Film setGenre(Film film) {
        final String sqlQueryGenre = "select G.GENRE_ID, G.GENRE_NAME from FILM_GENRES FG " +
                "left join GENRES G on G.GENRE_ID = FG.GENRE_ID " +
                "where FG.FILM_ID = ?";
        List<Genre> genres = jdbcTemplate.query(sqlQueryGenre, FilmDbStorage::makeGenre , film.getId());
        film.setGenres(genres);
        return film;
    }

    @Override
    public List<Film> findCommon(long userId, long friendId) {
        final String sqlQueryCommon = "select FILMS.*, MPA.* " +
                "from FILMS " +
                "join MPA on MPA.MPA_ID = FILMS.MPA_ID " +
                "where FILMS.FILM_ID " +
                "in (select distinct FILM_ID from LIKES where USER_ID = ? and ?)";

        List<Film> films = jdbcTemplate.query(sqlQueryCommon, FilmDbStorage::makeFilm, userId, friendId);
        if (films.size() == 0) {
            return Collections.emptyList();
        }
        for (Film film : films) {
            setGenre(film);
        }

        return films;
    }

    //МАППЕРЫ
    public static Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getInt("FILM_ID"))
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .mpa(new MPARating(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")))
                .build();
    }

    public static Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("GENRE_ID"), rs.getString("GENRE_NAME"));
    }

    public static MPARating makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MPARating(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"));
    }
}
