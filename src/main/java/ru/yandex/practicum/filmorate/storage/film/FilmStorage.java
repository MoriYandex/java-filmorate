package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {
    Film getFilm(Integer id);

    List<Film> getAllFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film addLike(Integer id, Integer userId);

    Film deleteLike(Integer id, Integer userId);

    List<Film> getMostPopular(Integer count);

    Film delete(Integer id);

    Set<Integer> getAllUserLikesById(Integer id);


    List<Film> getCommonFilms(Integer userId, Integer friendId);
}
