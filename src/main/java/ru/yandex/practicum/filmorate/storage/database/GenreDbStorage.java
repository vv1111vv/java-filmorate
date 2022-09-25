package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final String GET_ALL = "SELECT * FROM genres ORDER BY genre_id";
    private static final String GET_BY_ID = "SELECT * FROM GENRES WHERE genre_id = ?";

    private static final String GET_ALL_FILMS_GENRES =
            "SELECT * FROM FILM_GENRES fg, GENRES g WHERE g.genre_id = fg.genre_id AND fg.film_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query(GET_ALL, this::mapRowToGenre);
    }

    @Override
    public Genre get(Long id) {
        final List<Genre> genres = jdbcTemplate.query(GET_BY_ID, this::mapRowToGenre, id);
        return genres.size() > 0 ? genres.get(0) : null;
    }

    @Override
    public void loadGenreToFilms(List<Film> films) {
        for (Film film : films) {
            List<Genre> genres = jdbcTemplate.query(GET_ALL_FILMS_GENRES, this::mapRowToGenre, film.getId());
            if (genres.isEmpty()) {
                film.setGenres(new HashSet<>());
            } else {
                film.setGenres(new HashSet<>(genres));
            }
        }
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .title(rs.getString("title"))
                .build();
    }
}
