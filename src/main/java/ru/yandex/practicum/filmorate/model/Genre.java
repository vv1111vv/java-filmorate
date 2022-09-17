package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "title")
public class Genre {
    @JsonProperty("name")
    @NotBlank String title;
    private Integer id;

    @JsonCreator
    public static Genre forObject(@JsonProperty("id") int id, @JsonProperty String title) {
        return new Genre(title, id);
    }
}
