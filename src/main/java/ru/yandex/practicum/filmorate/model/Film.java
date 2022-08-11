package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Film {
    private long id;
    @NotBlank
    private String name;   //название не может быть пустым;
    @Length(max = 200)
    private String description;   //максимальная длина описания — 200 символов;
    private LocalDate releaseDate;   // дата релиза — не раньше 28 декабря 1895 года;
    @Positive
    private int duration;   //  продолжительность фильма должна быть положительной.
}
