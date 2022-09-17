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
            "SELECT * FROM FILMS f LEFT JOIN (SELECT film_id, COUNT(*) likes_count FROM likes"
                    + " GROUP BY film_id) l ON f.film_id = l.film_id LEFT JOIN mpa ON f.mpa_id = mpa.mpa_id"
                    + " ORDER BY l.likes_count DESC LIMIT ?";
    private static final String SAVE_LIKE = "INSERT INTO LIKES (user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM LIKES WHERE user_id = ? AND film_id = ?";
    private static final String GET_ALL_FILMS_GENRES =
            "SELECT * FROM FILM_GENRES INNER JOIN GENRES ON genres.genre_id = film_genres.genre_id";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> getPopularFilms(int limit) {
        final Map<Long, Set<Genre>> filmsGenres = getAllFilmsGenres();
        return jdbcTemplate.query(GET_POPULAR, (rs, numRow) -> {
            final Long filmId = rs.getLong("film_id");
            return mapRowToFilm(rs, filmsGenres.get(filmId));
        }, limit);
    }

    @Override
    public void saveLike(Like like) {
        jdbcTemplate.update(SAVE_LIKE, like.getUser().getId(), like.getFilm().getId());
    }

    @Override
    public void deleteLike(Like like) {
        jdbcTemplate.update(DELETE_LIKE, like.getUser().getId(), like.getFilm().getId());
    }

    private Map<Long, Set<Genre>> getAllFilmsGenres() {

        final Map<Long, Set<Genre>> filmsGenres = new HashMap<>();

        jdbcTemplate.query(GET_ALL_FILMS_GENRES, rs -> {
            final Long filmId = rs.getLong("film_id");
            filmsGenres.getOrDefault(filmId, new HashSet<>()).add(Genre.builder().id(rs.getInt("genre_id"))
                    .title(rs.getString("title")).build());
        });

        return filmsGenres;
    }

    private Film mapRowToFilm(ResultSet rs, Set<Genre> genres) throws SQLException {
        return new Film(rs.getLong("film_id"), rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new MPARating(rs.getInt("mpa_id"),
                        rs.getString("title")),
                genres != null && genres.isEmpty() ? null : genres);
    }
}
