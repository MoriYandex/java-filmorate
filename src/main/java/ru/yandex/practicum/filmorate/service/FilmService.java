package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortBy;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MOST_POPULAR_QUANTITY = 10;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;

    public Film getFilmById(Integer id) {
        log.info("Поиск фильма по идентификатору {}", id);
        Film film = filmStorage.get(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        return film;
    }

    public List<Film> getAllFilms() {
        log.info("Получение списка всех фильмов");
        return filmStorage.getAll();
    }

    public List<Film> getFilmsByDirId(Integer id, FilmSortBy by) {
        if (!directorStorage.exists(id)) {
            throw new NotFoundException(String.format("Режиссер с id %s не найден", id));
        }
        List<Film> allFilms = filmStorage.getAllByDirId(id);
        switch (by) {
            case likes:
                return allFilms.stream()
                        .sorted(Comparator.comparing(Film::getLikesCount))
                        .collect(Collectors.toList());
            case year:
                return allFilms.stream()
                        .sorted(Comparator.comparing(Film::getReleaseDate))
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    public Film addFilm(Film film) {
        log.info("Добавление фильма");
        validateFilm(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        log.info("Изменение данных фильма по идентификатору {}", film.getId());
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Film deleteFilm(Integer id) {
        log.info("Удаление фильма {}", id);
        return filmStorage.delete(id);
    }

    public Film addLike(Integer id, Integer userId) {
        log.info("Добавление лайка фильму {} пользователем {}", id, userId);
        User user = userStorage.get(userId);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", userId));
        return filmStorage.addLike(id, userId);
    }

    public Film deleteLike(Integer id, Integer userId) {
        if (id < 0 || userId < 0) {
            throw new NotFoundException("Пользователь не может быть с отрицательным id.");
        }
        log.info("Удаление лайка фильму {} пользователем {}", id, userId);
        User user = userStorage.get(userId);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        return filmStorage.deleteLike(id, userId);
    }

    public List<Film> getMostPopular(Integer count, Integer genreId, Integer year) {
        int filmCount = (count != null && count > 0) ? count : MOST_POPULAR_QUANTITY;
        log.info("Вывод {} наиболее популярных фильмов", filmCount);
        return filmStorage.getMostPopular(filmCount, genreId, year);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        log.info("Вывод общих фильмов пользователей {} и {}", userId, friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void validateFilm(Film film) {
        if (StringUtils.isBlank(film.getName())) {
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

    private List<User> getUsersMaxIntersectionByLikes(Integer id) {
        return userStorage.getMaxIntersectionUsers(id);
    }

    private Set<Integer> getDifference(User user1, User user2) {
        Set<Integer> difForRet = filmStorage.getAllUserLikesById(user2.getId());
        difForRet.removeAll(filmStorage.getAllUserLikesById(user1.getId()));
        return difForRet;
    }

    public List<Film> getRecommendFilms(Integer userId) {
        User user = userStorage.get(userId);
        if (user != null) {
            Set<Integer> idOfReturnFilms = new HashSet<>();
            List<User> intersectionByLikeUsers = getUsersMaxIntersectionByLikes(user.getId());
            for (User u : intersectionByLikeUsers) {
                idOfReturnFilms.addAll(getDifference(user, u));
            }
            return filmStorage.getAllByFilmIds(idOfReturnFilms);
        }
        return new ArrayList<>();
    }

    public List<Film> searchFilm(String query, String by) {
        log.info("Поиск по текстовой строке {} в полях {}.", query, by);
        if (StringUtils.isBlank(query) || StringUtils.isBlank(by) || !(by.contains("title") || by.contains("director")))
            throw new NotFoundException("Неверные параметры поиска фильма!");
        return filmStorage.search(query, by)
                .stream().sorted(Comparator.comparing(Film::getLikesCount).reversed())
                .collect(Collectors.toList());
    }
}
