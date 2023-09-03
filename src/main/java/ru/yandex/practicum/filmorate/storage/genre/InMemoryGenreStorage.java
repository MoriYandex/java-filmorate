package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("InMemoryGenreStorage")
@Slf4j
public class InMemoryGenreStorage implements GenreStorage {
    private final Map<Integer, Genre> allGenres = new HashMap<>();

    public InMemoryGenreStorage() {
        allGenres.put(1, new Genre(1, "Комедия"));
        allGenres.put(2, new Genre(2, "Драма"));
        allGenres.put(3, new Genre(3, "Мультфильм"));
        allGenres.put(4, new Genre(4, "Триллер"));
        allGenres.put(5, new Genre(5, "Документальный"));
        allGenres.put(6, new Genre(6, "Боевик"));
    }

    @Override
    public Genre getGenre(Integer id) {
        return allGenres.get(id);
    }

    @Override
    public List<Genre> getAllGenres() {
        return new ArrayList<>(allGenres.values());
    }

    @Override
    public List<Genre> getAllGenresByFilmId(Integer filmId) {
        return new ArrayList<>();
    }

    @Override
    public Map<Integer, List<Genre>> getMapOfGenresToFilms() {
        return new HashMap<>();
    }

    @Override
    public void load(List<Film> films) {
    }
}
