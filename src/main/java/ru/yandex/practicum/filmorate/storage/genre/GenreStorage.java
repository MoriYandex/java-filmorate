package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;

public interface GenreStorage {
    Genre get(Integer id);

    List<Genre> getAll();

    List<Genre> getAllByFilmId(Integer filmId);

    Map<Integer, List<Genre>> getMapOfGenresToFilms();
}
