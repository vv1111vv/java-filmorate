package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Slf4j
@Component
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    @NotBlank(message = "поле *login* не может быть пустым")
    @Pattern(regexp = "^[A-Za-z\\d]*$", message = "поле *login* не должно содержать пробелы и спец. символы")
    private String login;
    private String name;
    @NotBlank(message = "поле *email* не может быть пустым")
    @Email(message = "неверный формат поля *email*")
    private String email;
    @Past(message = "поле *birthday* не может указывать на будущую дату")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    public User(String login, String name, String email, LocalDate birthday) {
        this.login = login;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
    }

}