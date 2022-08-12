package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private long id; // целочисленный идентификатор — id;
    @NotNull
    @Email
    private String email; //  электронная почта не может быть пустой и должна содержать символ @;
    @NotBlank
    private String login; // логин не может быть пустым и содержать пробелы;
    private String name; //  имя для отображения может быть пустым — в таком случае будет использован логин;
    @NotNull
    @PastOrPresent
    private LocalDate birthday; // дата рождения не может быть в будущем.
}
