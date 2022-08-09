package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;


@Data
public class User {
    private long id; // целочисленный идентификатор — id;
    private String email; //  электронная почта не может быть пустой и должна содержать символ @;
    private String login; // логин не может быть пустым и содержать пробелы;
    private String name; //  имя для отображения может быть пустым — в таком случае будет использован логин;
    private LocalDate birthday; // дата рождения не может быть в будущем.
}
