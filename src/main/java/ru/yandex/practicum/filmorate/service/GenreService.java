package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
@Slf4j
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("DbGenreStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genre getGenreById(Integer id) {
        log.info(String.format("GenreService: Поиск жанра по идентификатору %d", id));
        Genre genre = genreStorage.getGenre(id);
        if (genre == null)
            throw new NotFoundException(String.format("Жанр %d не найден!", id));
        return genre;
    }

    public List<Genre> getAllGenres() {
        log.info("GenreService: Получение списка всех жанров");
        return genreStorage.getAllGenres();
    }
}
