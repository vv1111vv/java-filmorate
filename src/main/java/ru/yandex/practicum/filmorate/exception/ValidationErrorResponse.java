package ru.yandex.practicum.filmorate.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {
    @JsonProperty("errors")
    private final List<ValidationError> validationErrors;

    @Getter
    @RequiredArgsConstructor
    public static class ValidationError {
        @JsonProperty("field")
        private final String fieldName;

        @JsonProperty("message")
        private final String message;
    }
}