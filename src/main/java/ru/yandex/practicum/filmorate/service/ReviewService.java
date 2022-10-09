package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;

    // Сообщаем Spring, что нужно передать в конструктор объект класса ReviewStorage
    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage, FeedStorage feedStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;
    }

    public Review create(Review review) throws ObjectNotFoundException {
        validationReview(review);
        Review reviewOut = reviewStorage.create(review);
        feedStorage.addEvent(Event.builder()
                .userId(reviewOut.getUserId())
                .eventType("REVIEW")
                .operation("ADD")
                .timestamp(new Timestamp(System.currentTimeMillis()).getTime())
                .entityId(reviewOut.getReviewId())
                .build());
        return reviewOut;
    }

    public Review getById(int reviewId) throws ObjectNotFoundException {
        return reviewStorage.getById(reviewId);
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAllReviews();
    }

    public Review update(Review review) throws ObjectNotFoundException {
        validationReview(review);
        reviewStorage.update(review);
        Review reviewOut = getById(review.getReviewId());
        feedStorage.addEvent(Event.builder()
                .userId(reviewOut.getUserId())
                .eventType("REVIEW")
                .operation("UPDATE")
                .timestamp(new Timestamp(System.currentTimeMillis()).getTime())
                .entityId(reviewOut.getReviewId())
                .build());
        return reviewOut;
    }

    public void deleteById(int reviewId) throws ObjectNotFoundException {
        Review review = reviewStorage.getById(reviewId);
        feedStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("REMOVE")
                .timestamp(new Timestamp(System.currentTimeMillis()).getTime())
                .entityId(review.getReviewId())
                .build());
        reviewStorage.deleteById(reviewId);
    }

    public List<Review> getReviewsForFilm(Long filmId, int count) {
        return reviewStorage.getReviewsForFilm(filmId, count);
    }

    public void addLikeReview(Integer reviewId, Long userId) throws ObjectNotFoundException {
        validationReview(getById(reviewId));
        int useful = reviewStorage.getById(reviewId).getUseful();
        reviewStorage.updateLike(++useful, reviewId);
    }

    public void addDislikeReview(Integer reviewId, Long userId) throws ObjectNotFoundException {
        validationReview(getById(reviewId));
        int useful = reviewStorage.getById(reviewId).getUseful();
        reviewStorage.updateLike(--useful, reviewId);
    }

    public void deleteLikeFromReview(int reviewId, Long userId) throws ObjectNotFoundException {
        addDislikeReview(reviewId, userId);
    }

    public void deleteDislikeFromReview(int reviewId, Long userId) throws ObjectNotFoundException {
        addLikeReview(reviewId, userId);
    }

    /**
     * Валидация экземпляра класса Review.
     * @param review объект класса Review.
     * @throws ObjectNotFoundException исключение.
     */
    private void validationReview(Review review) throws ObjectNotFoundException {
        if (review.getFilmId() != null) {
            if (filmStorage.findById(review.getFilmId()) == null) {
                log.debug("Фильм не найден");
                throw new ValidationException("Фильм не найден");
            }
        } else {
            throw new ValidationException("У отзыва должно быть заполнено поле filmId");
        }
        if (review.getUserId() != null) {
            if (userStorage.findById(review.getUserId()) == null) {
                log.debug("Пользователь не найден");
                throw new ValidationException("Пользователь не найден");
            }
        } else {
            throw new ValidationException("У отзыва должно быть заполнено поле userId");
        }
        if (review.getIsPositive() == null) {
            log.debug("У отзыва должно быть заполнено поле isPositive");
            throw new ValidationException("У отзыва должно быть заполнено поле isPositive");
        }
    }
}
