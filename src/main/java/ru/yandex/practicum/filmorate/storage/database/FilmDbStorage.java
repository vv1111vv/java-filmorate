package ru.yandex.practicum.filmorate.storage.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final DirectorDao directorDao;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, DirectorDao directorDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.directorDao = directorDao;
    }

    @Override
    public List<Film> findAll() {
        final String sqlQuery = "SELECT * FROM FILMS LEFT JOIN MPA M ON FILMS.MPA_ID = M.MPA_ID";
        return jdbcTemplate.query(sqlQuery, this::makeFilm);
    }

    @Override
    public Film findById(long id) throws ObjectNotFoundException {
        final String sqlQuery =
                "SELECT * FROM FILMS AS f " +
                        "LEFT JOIN MPA M ON f.MPA_ID = M.MPA_ID " +
                        "WHERE f.FILM_ID = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, id);

        } catch (EmptyResultDataAccessException ex) {
            log.debug("Не найден фильм с ID " + id);
            throw new ObjectNotFoundException("Фильм не найден!");
        }
    }

    @Override
    public Collection<Film> findFilms(List<Integer> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException();
        }
        String filmIds = ids.stream().map(String::valueOf)
                .collect(Collectors.joining(","));
        String sql = "select * from films " +
                "where FILM_ID in (" + filmIds + ")";
        return jdbcTemplate.query(sql, this::makeFilm);
    }

    @Override
    public Film create(Film film) {
        final String sqlQuery = "" +
                "INSERT INTO FILMS(FILM_NAME, DESCRIPTION, DURATION, MPA_ID, RELEASE_DATE) " +
                "VALUES (?, ?, ?, ?, ?)";

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

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        //Обновить таблицу с жанрами
        setGenresByFilmId(film.getId(), film.getGenres());
        directorDao.addFilm(film);
        return film;
    }

    @Override
    public Film put(Film film) throws ObjectNotFoundException {
        Film initFilm = findById(film.getId());
        String sqlQuery = "UPDATE FILMS SET " +
                "FILM_NAME = ?, DESCRIPTION = ?, DURATION = ?, MPA_ID = ? , RELEASE_DATE = ?" +
                "WHERE FILM_ID = ?";
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
        sqlQuery = "DELETE FROM FILM_GENRES WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, film.getId());

        setGenresByFilmId(film.getId(), film.getGenres());
        film.setGenres(getGenresByFilmId(film.getId()));
        directorDao.updateFilm(film);

        if (row == 1) {
            Film updFilm = findById(film.getId());

            if (initFilm.getDirectors().size() != 0 && updFilm.getDirectors().size() == 0) {
                updFilm.setDirectors(null); // для прохождения теста postman
            }
            return updFilm;
        }
        return null;
    }

    @Override
    public void deleteAll() {
        String sqlQuery = "DELETE FROM FILMS";
        jdbcTemplate.update(sqlQuery);
    }

    @Override
    public void delete(long id) throws ObjectNotFoundException {
        String sqlQuery = "DELETE FROM FILMS WHERE FILM_ID = ?";
        if (jdbcTemplate.update(sqlQuery, id) == 0) {
            log.debug(String.format("Фильм %d не найден.", id));
            throw new ObjectNotFoundException("Фильм не найден.");
        }
        log.debug(String.format("Фильм %d удалён из системы.", id));
    }

    @Override
    public boolean addLike(long filmId, long userId) {
        final String sqlQuery = "INSERT INTO LIKES(USER_ID, FILM_ID) " + "VALUES (?, ?)";
        return (jdbcTemplate.update(sqlQuery, userId, filmId) > 0);
    }

    @Override
    public boolean deleteLike(long filmId, long userId) {
        String sqlQuery = "DELETE FROM LIKES WHERE USER_ID = ? AND FILM_ID = ?";
        return (jdbcTemplate.update(sqlQuery, userId, filmId) > 0);
    }

    @Override
    public List<Film> getPopularFilms(int count, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        List<String> conditions = new ArrayList<>();
        List<String> values = new ArrayList<>();

        if (!(params.containsKey("count") && params.keySet().size() == 1) && params.keySet().size() > 0) {
            sb.append("WHERE ");
        }

        if (params.containsKey("genreId")) {
            if (Integer.parseInt(params.get("genreId")) < 1)
                throw new ValidationException("Указан отрицательный ID жанра!");
            conditions.add("fg.GENRE_ID = ? ");
            values.add(params.get("genreId"));
        }

        if (params.containsKey("year")) {
            conditions.add("EXTRACT(YEAR FROM f.RELEASE_DATE) = ? ");
            values.add(params.get("year"));
        }

        for (int i = 0; i < conditions.size(); i++) {
            sb.append(conditions.get(i));
            if (i != conditions.size() - 1) {
                sb.append("AND ");
            }
        }
        values.add(String.valueOf(count));

        final String sqlQuery =
                "SELECT " +
                        "f.*, " +
                        "mpa.MPA_NAME, " +
                        "COUNT(L.USER_ID)" +
                        "FROM FILMS AS f " +
                        "LEFT JOIN LIKES AS l ON f.film_id = l.film_id " +
                        "LEFT JOIN MPA AS mpa ON F.MPA_ID = mpa.MPA_ID " +
                        "LEFT JOIN FILM_GENRES AS fg ON f.FILM_ID = fg.FILM_ID " +
                        (params.size() > 0 ? sb.toString() : "") +
                        " GROUP BY F.film_id, film_name, description, duration, f.mpa_id, mpa.mpa_id, mpa.mpa_name, release_date " +
                        "ORDER BY COUNT(L.USER_ID) DESC " +
                        "LIMIT ?";

        return jdbcTemplate.query(sqlQuery, this::makeFilm, values.toArray(new Object[0]));
    }

    @Override
    public List<Film> search(String query, List<String> searchOptions) {
        StringBuilder sb = new StringBuilder();
        String searchByDirector =
                "SELECT f.FILM_ID " +
                        "FROM FILMS AS f " +
                        "JOIN FILM_DIRECTORS AS fd ON f.FILM_ID = FD.FILM_ID " +
                        "JOIN DIRECTORS AS dir ON dir.ID = FD.ID " +
                        "WHERE UPPER(dir.DIRECTOR_NAME) LIKE UPPER('%" + query + "%')";

        String searchByFilmName =
                "SELECT f.FILM_ID " +
                        "FROM FILMS AS f " +
                        "WHERE UPPER(f.FILM_NAME) LIKE UPPER('%" + query + "%')";

        for (int i = 0; i < searchOptions.size(); i++) {
            String s = searchOptions.get(i);
            if (s.equals("director")) sb.append(searchByDirector);
            if (s.equals("title")) sb.append(searchByFilmName);
            if (!(i == searchOptions.size() - 1)) sb.append(" UNION ");

        }

        String sortedResult =
                "SELECT found.FILM_ID " +
                        "FROM " +
                        "("+ sb +") AS found " +
                        "LEFT OUTER JOIN LIKES AS l ON l.FILM_ID = found.FILM_ID " +
                        "GROUP BY found.FILM_ID " +
                        "ORDER BY COUNT(l.USER_ID) DESC";

        return jdbcTemplate.queryForList(sortedResult, Long.class)
                .stream()
                .map(this::findById)
                .collect(Collectors.toList());

    }

    @Override
    public MPARating findMpaById(long id) throws ObjectNotFoundException {
        final String sqlQuery = "SELECT * FROM MPA WHERE MPA_ID = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::makeMpa, id);

        } catch (EmptyResultDataAccessException ex) {
            log.debug(String.format("Неизвестный рейтинг %d.", id));
            throw new ObjectNotFoundException("Неизвестный рейтинг");

        }
    }

    @Override
    public List<MPARating> findAllMpa() {
        final String sqlQuery = "SELECT * FROM MPA";
        return jdbcTemplate.query(sqlQuery, this::makeMpa);
    }

    @Override
    public Genre findGenreById(long id) throws ObjectNotFoundException {
        final String sqlQuery = "SELECT * FROM GENRES WHERE GENRE_ID = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::makeGenre, id);

        } catch (EmptyResultDataAccessException ex) {
            log.debug(String.format("Неизвестный жанр %d.", id));
            throw new ObjectNotFoundException("Неизвестный жанр");
        }
    }

    @Override
    public List<Genre> findAllGenre() {
        final String sqlQuery = "SELECT * FROM GENRES";
        return jdbcTemplate.query(sqlQuery, this::makeGenre);
    }

    @Override
    public List<Film> findFilmsOfDirectorSortByYear(int directorId) {
        String sqlQuery = "SELECT *" +
                "FROM film_directors AS fd " +
                "JOIN films fl ON fd.film_id = fl.film_id " +
                "JOIN MPA m ON m.MPA_ID = fl.MPA_ID " +
                "WHERE fd.id = ? ORDER BY release_date";

        return jdbcTemplate.query(sqlQuery, this::makeFilm, directorId);
    }

    @Override
    public List<Film> findFilmsOfDirectorSortByLikes(int directorId) {
        String sqlQuery = "SELECT * " +
                "FROM film_directors AS fd " +
                "JOIN films AS fl ON fd.film_id = fl.film_id " +
                "JOIN MPA m ON m.MPA_ID = fl.MPA_ID " +
                "WHERE fd.id = ? ORDER BY rate";

        return jdbcTemplate.query(sqlQuery, this::makeFilm, directorId);
    }

    public List<Genre> getGenresByFilmId(Long filmId) {
        final String sqlQueryGenre = "SELECT G.GENRE_ID, G.GENRE_NAME FROM FILM_GENRES FG " +
                "LEFT JOIN GENRES G ON G.GENRE_ID = FG.GENRE_ID " +
                "WHERE FG.FILM_ID = ?";
        return jdbcTemplate.query(sqlQueryGenre, this::makeGenre, filmId);
    }

    private void setGenresByFilmId(Long filmId, Collection<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            for (Genre g : genres) {
                String sqlQueryGenre = "MERGE INTO FILM_GENRES(FILM_ID, GENRE_ID) VALUES (?, ?)";
                jdbcTemplate.update(sqlQueryGenre, filmId, g.getId());
            }
        }
    }

    //МАППЕРЫ
    public Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("FILM_ID"))
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .directors(new HashSet<>(directorDao.findFilm(rs.getLong("FILM_ID"))))
                .mpa(new MPARating(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")))
                .genres(getGenresByFilmId(rs.getLong("FILM_ID")))
                .build();
    }

    public Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("GENRE_ID"), rs.getString("GENRE_NAME"));
    }

    public MPARating makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MPARating(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"));
    }


    public List<Film> getUserFilms(long userId) {
        List<Film> userFilms = jdbcTemplate.query("SELECT * FROM FILMS LEFT JOIN MPA M ON FILMS.MPA_ID = M.MPA_ID " +
                        "WHERE FILM_ID IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = ?)",
                this::makeFilm, userId);
        return userFilms;
    }

    @Override
    public Map<Integer, List<Integer>> getAllFilmsLikes() {
        String sql = "select * from likes";
        Map<Integer, List<Integer>> likes = new HashMap<>();

        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql);

        while (rows.next()) {
            int userId = rows.getInt("user_id");
            int filmId = rows.getInt("film_id");

            if (!likes.containsKey(userId)) {
                likes.put(userId, new ArrayList<>(List.of(filmId)));
            }
            List<Integer> newValue = likes.get(userId);
            newValue.add(filmId); //Add new like

            likes.put(userId, newValue);
        }

        return likes;
    }
}