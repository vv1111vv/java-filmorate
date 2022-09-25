package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.database.*;

import java.util.Collection;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final LikeStorage likeStorage;

    private final GenreStorage genreStorage;



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
        List<Film> popularFilms = likeStorage.getPopularFilms(count != null ? count : 10);
        genreStorage.loadGenreToFilms(popularFilms);
        return popularFilms;
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public List<Film> getAll() {
        List<Film> allFilms = filmStorage.getAllFilms();
        genreStorage.loadGenreToFilms(allFilms);
        return allFilms;
    }

    public Film getById(Long id) {
        Film film = filmStorage.getById(id);
        genreStorage.loadGenreToFilms(List.of(film));
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

