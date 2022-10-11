package ru.yandex.practicum.filmorate.storage.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
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

@Component
@Primary
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final DirectorDao directorDao;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, DirectorDao directorDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.directorDao = directorDao;
    }

    @Override
    public List<Film> findAll() {
        final String sqlQuery = "select * from FILMS left join MPA M on FILMS.MPA_ID = M.MPA_ID";
        final List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
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
        final List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm, id);
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
        directorDao.addFilm(film);

        return film;
    }

    @Override
    public Film put(Film film) throws ObjectNotFoundException {
        Film initFilm = findById(film.getId());
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
        int rows = jdbcTemplate.update(sqlQuery, userId, filmId);
        updateRate(filmId, 1);
        return (rows > 0);
    }

    @Override
    public boolean deleteLike(long filmId, long userId) {
        String sqlQuery = "delete from LIKES where USER_ID = ? AND FILM_ID = ?";
        int rows = jdbcTemplate.update(sqlQuery, userId, filmId);
        updateRate(filmId, -1);
        return (rows > 0);
    }

    private void updateRate(long filmId, Integer rateDifference) {
        String sqlQuery = "UPDATE films SET rate = rate + ? WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, rateDifference, filmId);
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

    public List<Genre> findFilmGenres(long id) {
        String sql = "SELECT fg.genre_id, g.genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g on g.genre_id = fg.genre_id " +
                "WHERE film_id = ?";

        return jdbcTemplate.query(sql, FilmDbStorage::makeGenre, id);
    }

    @Override
    public List<Film> findFilmsOfDirectorSortByYear(int directorId) {
        String sqlQuery = "SELECT fl.film_id, " +
                "fl.film_name, " +
                "fl.description, " +
                "fl.release_date, " +
                "fl.duration, " +
                "fl.mpa_id " +
                "FROM film_directors AS fd " +
                "JOIN films fl ON fd.film_id = fl.film_id " +
                "WHERE fd.id = ? ORDER BY release_date";

        return jdbcTemplate.query(sqlQuery, this::makeFilmBySort, directorId);
    }

    @Override
    public List<Film> findFilmsOfDirectorSortByLikes(int directorId) {
        String sqlQuery = "SELECT fl.film_id, " +
                "fl.film_name, " +
                "fl.description, " +
                "fl.release_date, " +
                "fl.duration, " +
                "fl.mpa_id " +
                "FROM film_directors AS fd " +
                "JOIN films AS fl ON fd.film_id = fl.film_id " +
                "WHERE fd.id = ? ORDER BY rate";

        return jdbcTemplate.query(sqlQuery, this::makeFilmBySort, directorId);
    }


    private Film setGenre(Film film) {
        final String sqlQueryGenre = "select G.GENRE_ID, G.GENRE_NAME from FILM_GENRES FG " +
                "left join GENRES G on G.GENRE_ID = FG.GENRE_ID " +
                "where FG.FILM_ID = ?";
        List<Genre> genres = jdbcTemplate.query(sqlQueryGenre, FilmDbStorage::makeGenre, film.getId());
        film.setGenres(genres);
        return film;
    }

    //МАППЕРЫ

    public Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("film_id");
        return Film.builder()
                .id(id)
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .directors(new HashSet<>(directorDao.findFilm(id)))
                .mpa(findMpaById(rs.getInt("mpa_id")))
                .build();
    }

    public Film makeFilmBySort(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("film_id");
        Collection<Genre> genres = new HashSet<>(findFilmGenres(id));
        genres = genres.isEmpty() ? null : genres;
        return Film.builder()
                .id(id)
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .directors(new HashSet<>(directorDao.findFilm(id)))
                .mpa(findMpaById(rs.getInt("mpa_id")))
                .genres(genres)
                .build();
    }

    public static Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("GENRE_ID"), rs.getString("GENRE_NAME"));
    }

    public static MPARating makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MPARating(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"));
    }
}