package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@RequestBody Review review) throws ObjectNotFoundException {
        return reviewService.create(review);
    }

    @GetMapping("/{reviewId}")
    public Review getById(@PathVariable int reviewId) throws ObjectNotFoundException {
        return reviewService.getById(reviewId);
    }

    @PutMapping
    public Review update(@RequestBody Review review) throws ObjectNotFoundException {
        return reviewService.update(review);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteById(@PathVariable int reviewId) throws ObjectNotFoundException {
        reviewService.deleteById(reviewId);
    }

    @GetMapping
    public List<Review> getReviewsForFilm(
            @RequestParam(value = "filmId", defaultValue = "-1", required = false) Long filmId,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count) {
        if (filmId == -1) {
            return reviewService.getAllReviews();
        }
        return reviewService.getReviewsForFilm(filmId, count);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLikeReview(@PathVariable int reviewId, @PathVariable Long userId) throws ObjectNotFoundException {
        reviewService.addLikeReview(reviewId, userId);
        log.info("Для отзыва с ID: {} добавлен лайк от пользователя c ID: {}", reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislikeReview(@PathVariable int reviewId, @PathVariable Long userId) throws ObjectNotFoundException {
        reviewService.addDislikeReview(reviewId, userId);
        log.info("Для отзыва с ID: {} добавлен дизлайк от пользователя c ID: {}", reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void removeLikeFromReview(@PathVariable int reviewId, @PathVariable Long userId) throws ObjectNotFoundException {
        reviewService.deleteLikeFromReview(reviewId, userId);
        log.info("Для отзыва с ID: {} удален лайк от пользователя c ID: {}", reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void removeDislikeFromReview(@PathVariable int reviewId, @PathVariable Long userId) throws ObjectNotFoundException {
        reviewService.deleteDislikeFromReview(reviewId, userId);
        log.info("Для отзыва с ID: {} удален дизлайк от пользователя c ID: {}", reviewId, userId);
    }
}