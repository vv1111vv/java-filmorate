package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.database.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.database.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.database.LikeStorage;

import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final UserService userService;
    private final LikeStorage likeStorage;

    @Autowired
    FilmService(FilmDbStorage FilmDbStorage, LikeDbStorage databaseLikeStorage,
                UserService userService) {
        this.filmStorage = FilmDbStorage;
        this.userService = userService;
        this.likeStorage = databaseLikeStorage;
    }

    public void saveLike(Long filmId, Long userId) {

        likeStorage.saveLike(Like
                .builder()
                .film(filmStorage.getById(filmId))
                .user(userService.getById(userId))
                .build());
    }

    public void deleteLike(Long filmId, Long userId) {

        likeStorage.deleteLike(Like
                .builder()
                .film(filmStorage.getById(filmId))
                .user(userService.getById(userId))
                .build());
    }

    public Collection<Film> getPopularFilms(Integer count) {
        return likeStorage.getPopularFilms(count != null ? count : 10);
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAllFilms();
    }

    public Film getById(Long id) {
        Film film = filmStorage.getById(id);
        return film;
    }

    public Film update(Film newFilm) {
        final Film oldFilm = getById(newFilm.getId());
        if (oldFilm.equals(newFilm)) return newFilm;
        return filmStorage.update(newFilm);
    }

    public void deleteFilm(Film film) {
        filmStorage.deleteFilm(film);
    }

}

