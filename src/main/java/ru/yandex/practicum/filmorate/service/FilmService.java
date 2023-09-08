package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
        log.info(String.format("FilmService: Поиск фильма по идентификатору %d", id));
        Film film = filmStorage.get(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        return film;
    }

    public List<Film> getAllFilms() {
        log.info("FilmService: Получение списка всех фильмов");
        return filmStorage.getAll();
    }

    public List<Film> getFilmsByDirId(Integer id, FilmSortBy by) {
        if (!directorStorage.exists(id)) {
            throw new NotFoundException(String.format("Режиссер с id %s не найден", id));
        }
        List<Film> allFilms = filmStorage.getAll();
        switch (by) {

            case likes:
                return allFilms.stream()
                        .filter(p1 -> !p1.getDirectors().isEmpty() && p1.getDirectors().get(0).getId() == id)
                        .sorted(Comparator.comparing(Film::getLikesCount))
                        .collect(Collectors.toList());


            case year:
                return allFilms.stream()
                        .filter(p1 -> !p1.getDirectors().isEmpty() && p1.getDirectors().get(0).getId() == id)
                        .sorted(Comparator.comparing(Film::getReleaseDate))
                        .collect(Collectors.toList());


            default:
                return new ArrayList<>();

        }
    }

    public Film addFilm(Film film) {
        log.info("FilmService: Добавление фильма");
        validateFilm(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        log.info(String.format("FilmService: Изменение данных фильма по идентификатору %d", film.getId()));
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Film deleteFilm(Integer id) {
        log.info("FilmService: Удаление фильма");
        return filmStorage.delete(id);
    }

    public Film addLike(Integer id, Integer userId) {
        log.info(String.format("FilmService: Добавление лайка фильму %d пользователем %d", id, userId));
        User user = userStorage.get(userId);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", userId));
        return filmStorage.addLike(id, userId);
    }

    public Film deleteLike(Integer id, Integer userId) {
        if (id < 0 || userId < 0) {
            throw new NotFoundException("Пользователь не может быть с отрицательным id.");
        }
        log.info(String.format("FilmService: Удаление лайка фильму %d пользователем %d", id, userId));
        User user = userStorage.get(userId);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        return filmStorage.deleteLike(id, userId);
    }

    public List<Film> getMostPopular(Integer count, Integer genreId, Integer year) {
        int filmCount = (count != null && count > 0) ? count : MOST_POPULAR_QUANTITY;
        log.info(String.format("FilmService: Вывод %d наиболее популярных фильмов", filmCount));
        return filmStorage.getMostPopular(filmCount, genreId, year);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        log.info(String.format("FilmService: Вывод общих фильмов пользователей %d и %d", userId, friendId));
        return filmStorage.getCommonFilms(userId, friendId);
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

    private List<User> getUsersMaxIntersectionByLikes(Integer id) {

        Set<Integer> userLikes = filmStorage.getAllUserLikesById(id);

        List<User> allUsers = userStorage.getAll();

        List<User> usersForReturn = new ArrayList<>();

        int maxIntersection = 0;

        for (User other : allUsers) {

            userLikes.retainAll(filmStorage.getAllUserLikesById(other.getId()));

            if (userLikes.size() > maxIntersection) {

                maxIntersection = userLikes.size();

                usersForReturn.clear();
                usersForReturn.add(other);

            } else if (userLikes.size() == maxIntersection) {
                usersForReturn.add(other);
            }

        }
        return usersForReturn;
    }

    private Set<Integer> getDifference(User user1, User user2) {

        Set<Integer> difForRet = filmStorage.getAllUserLikesById(user2.getId());
        difForRet.removeAll(filmStorage.getAllUserLikesById(user1.getId()));

        return difForRet;
    }

    public List<Film> getRecommendFilms(Integer idOfUser) {

        User user = userStorage.get(idOfUser);
        if (user != null) {

            Set<Integer> idOfReturnFilms = new HashSet<>();

            List<User> intersectionByLikeUsers = getUsersMaxIntersectionByLikes(user.getId());


            for (User u : intersectionByLikeUsers) {
                idOfReturnFilms.addAll(getDifference(user, u));
            }
            List<Film> allFilms = filmStorage.getAll();
            List<Film> filmsForRet = new ArrayList<>();

            for (Film f : allFilms) {
                if (idOfReturnFilms.contains(f.getId())) {
                    filmsForRet.add(f);
                }
            }
            return filmsForRet;
        }
        return new ArrayList<>();
    }

    public List<Film> searchFilm(String query, String by) {

        List<Film> allFilms = filmStorage.getAll();

        List<Film> forRet = new ArrayList<>();

        if (by.contains("title") && !by.contains("director")) {

            forRet = allFilms.stream()
                    .filter(p1 -> p1.getName().toLowerCase().contains(query.toLowerCase()))
                    .sorted(Comparator.comparing(Film::getLikesCount).reversed())
                    .collect(Collectors.toList());

        } else if (!by.contains("title") && by.contains("director")) {

            forRet = allFilms.stream()
                    .filter(p1 -> !p1.getDirectors().isEmpty() && p1.getDirectors().get(0).getName()
                            .toLowerCase().contains(query.toLowerCase()))
                    .sorted(Comparator.comparing(Film::getLikesCount).reversed())
                    .collect(Collectors.toList());

        } else if (by.contains("title") && by.contains("director")) {

            forRet = allFilms.stream()
                    .filter(f -> (((!f.getDirectors().isEmpty()) && (f.getDirectors().get(0).getName().toLowerCase().contains(query.toLowerCase()))) || f.getName().toLowerCase().contains(query.toLowerCase())))
                    .sorted(Comparator.comparing(Film::getLikesCount).reversed())
                    .collect(Collectors.toList());
        }
        return forRet;
    }
}
