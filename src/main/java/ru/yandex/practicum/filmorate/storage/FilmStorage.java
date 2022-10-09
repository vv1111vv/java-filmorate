package ru.yandex.practicum.filmorate.storage;


import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.util.List;

public interface FilmStorage {

    List<Film> findAll();
    Film findById(long id) throws ObjectNotFoundException;
    Film create(Film film);
    Film put(Film film) throws ObjectNotFoundException;
    void deleteAll();
    void delete(long filmId) throws ObjectNotFoundException;
    boolean addLike(long filmId, long userId);
    boolean deleteLike(long filmId, long userId);
    List<Film> getPopularFilms(int count);

    MPARating findMpaById(long id) throws ObjectNotFoundException;

    List<MPARating> findAllMpa();

    Genre findGenreById(long id) throws ObjectNotFoundException;

    List<Genre> findAllGenre();

    List<Film> findCommon(long idUser, long idFriend);
}
