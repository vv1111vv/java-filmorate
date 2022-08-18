package ru.yandex.practicum.filmorate.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/*
Класс отвечает за операции с фильмами, — добавление и удаление лайка, вывод 10 наиболее популярных
фильмов по количеству лайков.
 */
@Service
@Slf4j
@NoArgsConstructor
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private long idgenerator;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    //создать фильм
    public Film create(Film film) {
        ++idgenerator;
        film.setId(idgenerator);
        return filmStorage.create(film);
    }

    //обновленить фильм;
    public Film update(Film film) {
        return filmStorage.update(film);
    }

    //полученить список всех фильмов
    public ArrayList<Film> getFilms() {
        return filmStorage.getFilms();
    }

    //получить фильм по id
    public Film getFilmById(long id) {
        Film film = filmStorage.getFilmById(id);
        return film;
    }

    //добавить like
    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        film.getLikes().add(user.getId());
        update(film);
    }

    //удалить like
    public void deleteLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        film.getLikes().remove(user.getId());
        update(film);
    }

    //получить список Топ-10 фильмов
    public Set<Film> topFilm(int count) {
        ArrayList<Film> films = filmStorage.getFilms();
        Set<Film> collect = new HashSet<>();

        films.stream()
                .sorted(Comparator.comparingInt(Film::getCountLike).reversed())
                .limit(count)
                .forEach(collect::add);

        return collect;
    }
}

