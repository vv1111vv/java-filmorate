package ru.yandex.practicum.filmorate.storage.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.LikeAlreadyExistsException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Component("reviewDbStorage")
@Slf4j
public class ReviewDbStorage implements ReviewStorage {

    private static final String SQL_UPDATE_REVIEW =
            "UPDATE REVIEWS SET CONTENT=?, IS_POSITIVE=? WHERE REVIEW_ID=?";
    private static final String SQL_GET_REVIEW_BY_ID = "SELECT * FROM REVIEWS WHERE REVIEW_ID=?";
    private static final String SQL_GET_ALL_REVIEW = "SELECT * FROM REVIEWS";
    private static final String SQL_DELETE_REVIEW = "DELETE FROM REVIEWS WHERE REVIEW_ID=?";
    private static final String SQL_GET_REVIEWS_FOR_FILM = "SELECT * FROM REVIEWS WHERE FILM_ID=? ORDER BY useful DESC LIMIT ?";
    private static final String SQL_UPDATE_USEFUL = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?";
    private static final String SQL_GET_REVIEW = "SELECT * FROM REVIEWS_USERS WHERE review_id=? AND user_id=?";

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
    }

    @Override
    public Review create(Review review) {
        if (review.getReviewId() == null) {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("reviews")
                    .usingGeneratedKeyColumns("review_id");
            review.setReviewId(simpleJdbcInsert.executeAndReturnKey(review.toMap()).intValue());
            log.info("(БД) Успешно добавлен отзыв c ID: {}", review.getReviewId());
            return review;
        } else {
            jdbcTemplate.update(SQL_UPDATE_REVIEW,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUserId(),
                    review.getFilmId(),
                    review.getUseful(),
                    review.getReviewId());
            log.info("(БД) Успешно обновлен отзыв c ID: {}", review.getReviewId());
        }
        return review;
    }

    @Override
    public Review getById(int reviewId) throws ObjectNotFoundException {
        final List<Review> review = jdbcTemplate.query(SQL_GET_REVIEW_BY_ID, (this::makeReview), reviewId);
        if (review.size() == 0) {
            log.debug("В storage не содержится отзыв с ID: {}", reviewId);
            throw new ObjectNotFoundException("В storage не содержится отзыв");
        }
        return review.get(0);
    }

    @Override
    public List<Review> getAllReviews() {
        return jdbcTemplate.query(SQL_GET_ALL_REVIEW, (this::makeReview)).stream()
                .sorted((o1, o2) -> {
                    int result = Integer.compare(o1.getUseful(), o2.getUseful());
                    return result * -1;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Review update(Review review) throws ObjectNotFoundException {
        if (review.getReviewId() == null) {
            log.debug("В storage не содержится отзыв с ID: {}", review.getReviewId());
            throw new ObjectNotFoundException("В storage не содержится отзыв");
        } else if (getById(review.getReviewId()) == null) {
            log.debug("В storage не содержится отзыв с ID: {}", review.getReviewId());
            throw new ObjectNotFoundException("В storage не содержится отзыв");
        }
        jdbcTemplate.update(SQL_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        log.info("(БД) Успешно обновлен отзыв c ID: {}", review.getReviewId());
        return review;
    }

    @Override
    public void deleteById(int reviewId) throws ObjectNotFoundException {
        if (reviewId == 0) {
            log.debug("В storage не содержится отзыв с ID: {}", reviewId);
            throw new ObjectNotFoundException("В storage не содержится отзыв");
        } else {
            jdbcTemplate.update(SQL_DELETE_REVIEW, reviewId);
            log.info("(БД) Успешно удален отзыв c ID: {}", reviewId);
        }
    }

    @Override
    public List<Review> getReviewsForFilm(Long filmId, int count) {
        return jdbcTemplate.query(SQL_GET_REVIEWS_FOR_FILM, (this::makeReview), filmId, count).stream()
                .sorted((o1, o2) -> {
                    int result = Integer.compare(o1.getUseful(), o2.getUseful());
                    return result * -1;
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(Integer reviewId, Long userId) {
        SqlRowSet review = jdbcTemplate.queryForRowSet(SQL_GET_REVIEW, reviewId, userId);
        if (review.next()) {
            log.debug("Пользователь с ID: {} уже добавил лайк отзыву с ID: {}", userId, reviewId);
            throw new LikeAlreadyExistsException("Невозможно добавить более 1 лайка для отзыва от пользователя");
        }
        jdbcTemplate.update(SQL_UPDATE_USEFUL, getById(reviewId).getUseful() + 1, reviewId);
        jdbcTemplate.update("UPDATE REVIEWS_USERS SET  review_id= ?, user_id = ?, is_like = ?;", reviewId, userId, true);
    }

    @Override
    public void addDislike(Integer reviewId, Long userId) {
        SqlRowSet review = jdbcTemplate.queryForRowSet(SQL_GET_REVIEW, reviewId, userId);
        if (review.next()) {
            log.debug("Пользователь с ID: {} уже добавил дизлайк отзыву с ID: {}", userId, reviewId);
            throw new LikeAlreadyExistsException("Невозможно добавить более 1 дизлайка для отзыва от пользователя");
        }
        jdbcTemplate.update(SQL_UPDATE_USEFUL, getById(reviewId).getUseful() - 1, reviewId);
        jdbcTemplate.update("UPDATE reviews_users SET  review_id= ?, user_id = ?, is_like = ?;", reviewId, userId, false);
    }

    @Override
    public void removeLike(Integer reviewId, Long userId) {
        jdbcTemplate.update(SQL_UPDATE_USEFUL, getById(reviewId).getUseful() - 1, reviewId);
        jdbcTemplate.update("DELETE FROM reviews_users WHERE review_id = ? AND user_id = ? AND is_like = ?", reviewId, userId, true);
    }

    @Override
    public void removeDislike(Integer reviewId, Long userId) {
        jdbcTemplate.update(SQL_UPDATE_USEFUL, getById(reviewId).getUseful() + 1, reviewId);
        jdbcTemplate.update("DELETE FROM reviews_users WHERE review_id = ? AND user_id = ? AND is_like = ?", reviewId, userId, false);
    }

    /**
     * Маппер для класса Review.
     * @param resultSet строка из БД.
     * @param i количество.
     * @return объект класса Review.
     * @throws SQLException исключение.
     */
    private Review makeReview(ResultSet resultSet, int i) throws SQLException {
        if (resultSet.getRow() == 0) {
            log.debug("Невозможно сформировать объект класса из пустой строки");
            throw new ObjectNotFoundException("В storage не содержится отзыв");
        }
        return Review.builder()
                .reviewId(resultSet.getInt("review_id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .useful(resultSet.getInt("useful"))
                .build();
    }
}
