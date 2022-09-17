package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final String GET_ALL = "SELECT * FROM genres ORDER BY genre_id";
    private static final String GET_BY_ID = "SELECT * FROM GENRES WHERE genre_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query(GET_ALL, (rs, rowNum) -> mapRowToGenre(rs));
    }

    @Override
    public Genre get(Long id) {
        final List<Genre> genres = jdbcTemplate.query(GET_BY_ID, (rs, rowNum) -> mapRowToGenre(rs), id);
        return genres.size() > 0 ? genres.get(0) : null;
    }

    private Genre mapRowToGenre(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .title(rs.getString("title"))
                .build();
    }
}
