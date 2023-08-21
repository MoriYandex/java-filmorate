package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    Genre getGenre(Integer id);

    List<Genre> getAllGenres();

    List<Genre> getAllGenresByFilmId(Integer filmId);
}
