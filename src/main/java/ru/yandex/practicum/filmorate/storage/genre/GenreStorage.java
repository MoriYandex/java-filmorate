package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;

public interface GenreStorage {
    Genre getGenre(Integer id);

    List<Genre> getAllGenres();

    List<Genre> getAllGenresByFilmId(Integer filmId);

    Map<Integer, List<Genre>> getMapOfGenresToFilms();

    void load(List<Film> films);

}
