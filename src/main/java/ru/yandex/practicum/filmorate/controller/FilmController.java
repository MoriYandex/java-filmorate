package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final TreeMap<Integer, Film> allFilms = new TreeMap<>();
    private Integer filmIdSequence = 0;

    @GetMapping()
    public List<Film> getAllFilms() {
        String FILMS_LIST_GOT_MESSAGE = "Получен список всех фильмов.";
        log.info(FILMS_LIST_GOT_MESSAGE);
        return new ArrayList<>(allFilms.values());
    }

    @PostMapping()
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);
        film.setId(++filmIdSequence);
        allFilms.put(film.getId(), film);
        String FILM_ADDED_MESSAGE = "Фильм %d успешно добавлен.";
        log.info(String.format(FILM_ADDED_MESSAGE, film.getId()));
        return film;
    }

    @PutMapping()
    public Film updateFilm(@RequestBody Film film) {
        validateFilm(film);
        if (!allFilms.containsKey(film.getId()))
            film.setId(++filmIdSequence);
        allFilms.put(film.getId(), film);
        String FILM_UPDATED_MESSAGE = "Фильм %d успешно изменён.";
        log.info(FILM_UPDATED_MESSAGE, film.getId());
        return film;
    }

    public void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            String EMPTY_NAME_MESSAGE = "Название не должно быть пустым!";
            log.error(EMPTY_NAME_MESSAGE);
            throw new ValidationException(EMPTY_NAME_MESSAGE);
        }
        int MAX_DESCRIPTION_LENGTH = 200;
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            String TOO_LONG_DESCRIPTION_MESSAGE = "Описание не должно быть длиннее %d символов!";
            String errorMessage = String.format(TOO_LONG_DESCRIPTION_MESSAGE, MAX_DESCRIPTION_LENGTH);
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            String TOO_OLD_MESSAGE = "Дата релиза не может быть ранее %s!";
            String errorMessage = String.format(TOO_OLD_MESSAGE, MIN_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getDuration() <= 0) {
            String NEGATIVE_DURATION_MESSAGE = "Продолжительность фильма должна быть положительной!";
            log.error(NEGATIVE_DURATION_MESSAGE);
            throw new ValidationException(NEGATIVE_DURATION_MESSAGE);
        }
    }
}
