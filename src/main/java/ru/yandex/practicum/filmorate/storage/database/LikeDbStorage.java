package ru.yandex.practicum.filmorate.storage.database;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Data
public class LikeDbStorage implements LikeStorage {
    private static final String GET_POPULAR =
            //"select * from FILMS f, MPA m where f.mpa_id = m.mpa_id order by likes_count desc limit ?";

            "SELECT * FROM FILMS f LEFT JOIN (SELECT film_id, COUNT(*) likes_count FROM likes"
                    + " GROUP BY film_id) l ON f.film_id = l.film_id LEFT JOIN mpa ON f.mpa_id = mpa.mpa_id"
                    + " ORDER BY l.likes_count DESC LIMIT ?";
    private static final String SAVE_LIKE = "INSERT INTO LIKES (user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM LIKES WHERE user_id = ? AND film_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getPopularFilms(int limit) {
        return jdbcTemplate.query(GET_POPULAR, this::mapToFilm, limit);
    }

    @Override
    public void saveLike(Like like) {
        jdbcTemplate.update(SAVE_LIKE, like.getUser().getId(), like.getFilm().getId());
    }

    @Override
    public void deleteLike(Like like) {
        jdbcTemplate.update(DELETE_LIKE, like.getUser().getId(), like.getFilm().getId());
    }

    private Film mapToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));
        film.setMpa(new MPARating(resultSet.getInt("mpa_id"), resultSet.getString("title")));
        return film;
    }

    @Override
    public List<Film> findAll() {
        String sql =
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, r.title " +
                        "FROM FILMS f JOIN MPA r ON f.mpa_id = r.mpa_id ORDER BY f.film_id";
        return jdbcTemplate.query(sql, this::mapToFilm);
    }

    private void updateRate(long filmId) {
        String sqlQuery = "update FILMS f set rate = (select count(l.user_id) from LIKES l where l.film_id = f.film_id)  where film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }
}
