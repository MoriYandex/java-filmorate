package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final static int MOST_POPULAR_QUANTITY = 10;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film getFilmById(Integer id) {
        Film film = filmStorage.getFilm(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        return film;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        return filmStorage.updateFilm(film);
    }

    public Film addLike(Integer id, Integer userId) {
        User user = userStorage.getUser(userId);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        return filmStorage.addLike(id, userId);
    }

    public Film deleteLike(Integer id, Integer userId) {
        User user = userStorage.getUser(userId);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        return filmStorage.deleteLike(id, userId);
    }

    public List<Film> getMostPopular(Integer count) {
        return filmStorage.getMostPopular((count != null && count > 0) ? count : MOST_POPULAR_QUANTITY);
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
