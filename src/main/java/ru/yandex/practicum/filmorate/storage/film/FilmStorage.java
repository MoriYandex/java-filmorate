package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film get(Integer id);

    List<Film> getAll();

    List<Film> getAllByDirId(Integer dirId);

    List<Film> search(String query, String by);

    Film add(Film film);

    Film update(Film film);

    Film delete(Integer id);

    Film addLike(Integer id, Integer userId);

    Film deleteLike(Integer id, Integer userId);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    List<Film> getMostPopular(Integer count, Integer genreId, Integer year);

    List<Film> getRecommendFilms(Integer userId);
}
