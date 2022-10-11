package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Review {
    private Integer reviewId;
    @NotBlank(message = "Поле content не может быть пустым")
    private String content;
    @NotBlank(message = "Поле isPositive не может быть пустым")
    private Boolean isPositive;
    @NotBlank(message = "Поле userId не может быть пустым")
    private Long userId;
    @NotBlank(message = "Поле filmId не может быть пустым")
    private Long filmId;
    private int useful;

    public Map<String, Object> toMap() {
        Map<String, Object> reviewsMap = new HashMap<>();
        reviewsMap.put("review_id", reviewId);
        reviewsMap.put("content", content);
        reviewsMap.put("is_positive", isPositive);
        reviewsMap.put("user_id", userId);
        reviewsMap.put("film_id", filmId);
        reviewsMap.put("useful", useful);
        return reviewsMap;
    }
}
