package ru.yandex.practicum.filmorate.storage.film;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.ArrayList;

/*
Интерфейс определяет методы:
- создать фильм
- обновить фильм
- получить список фильмов
 */

@Component
public interface FilmStorage {
    //создать фильм
    Film create(Film film);

    //обновить фильм
    Film update(Film film);

    //получить список фильмов
    ArrayList<Film> getFilms();
}
