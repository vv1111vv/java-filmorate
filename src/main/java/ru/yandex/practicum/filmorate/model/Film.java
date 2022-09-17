package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.utils.IsAfter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;


@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200, message = "допустимый размер описания: 200 символов")
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @IsAfter(current = "1895-12-28", message = "до 28 декабря 1895 года фильмов не существовало")
    private LocalDate releaseDate;
    @Min(0)
    private int duration;
    @NonNull
    private MPARating mpa;
    private Set<Genre> genres;


    public Film(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
