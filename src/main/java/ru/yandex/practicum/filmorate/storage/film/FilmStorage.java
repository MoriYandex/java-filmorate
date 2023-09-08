package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {
    Film get(Integer id);

    List<Film> getAll();

    Film add(Film film);

    Film update(Film film);

    Film delete(Integer id);

    Film addLike(Integer id, Integer userId);

    Film deleteLike(Integer id, Integer userId);

    Set<Integer> getAllUserLikesById(Integer id);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    List<Film> getMostPopular(Integer count, Integer genreId, Integer year);
}
