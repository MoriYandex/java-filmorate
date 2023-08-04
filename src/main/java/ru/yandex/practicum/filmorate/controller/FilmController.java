package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Integer, Film> allFilms = new TreeMap<>();
    private Integer filmIdSequence = 0;

    @GetMapping()
    public List<Film> getAllFilms() {
        log.info("Получен список всех фильмов.");
        return new ArrayList<>(allFilms.values());
    }

    @PostMapping()
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);
        film.setId(++filmIdSequence);
        allFilms.put(film.getId(), film);
        log.info(String.format("Фильм %d успешно добавлен.", film.getId()));
        return film;
    }

    @PutMapping()
    public Film updateFilm(@RequestBody Film film) {
        validateFilm(film);
        if (!allFilms.containsKey(film.getId()))
            throw new ValidationException("Фильм не найден!");
        allFilms.put(film.getId(), film);
        log.info(String.format("Фильм %d успешно изменён.", film.getId()));
        return film;
    }

    public void validateFilm(Film film) {
        if (!StringUtils.hasText(film.getName())) {
            String emptyNameMessage = "Название не должно быть пустым!";
            log.error(emptyNameMessage);
            throw new ValidationException(emptyNameMessage);
        }
        int maxDescriptionLength = 200;
        if (film.getDescription() != null && film.getDescription().length() > maxDescriptionLength) {
            String errorMessage = String.format("Описание не должно быть длиннее %d символов!", maxDescriptionLength);
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            String tooOldMessage = "Дата релиза не может быть ранее %s!";
            String errorMessage = String.format(tooOldMessage, MIN_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getDuration() <= 0) {
            String negativeDurationMessage = "Продолжительность фильма должна быть положительной!";
            log.error(negativeDurationMessage);
            throw new ValidationException(negativeDurationMessage);
        }
    }
}
