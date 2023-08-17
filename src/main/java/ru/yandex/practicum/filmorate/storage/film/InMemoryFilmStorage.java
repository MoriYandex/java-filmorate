package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> allFilms = new TreeMap<>();
    private Integer filmIdSequence = 0;

    @Override
    public Film getFilm(Integer id) {
        return allFilms.get(id);
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Получен список всех фильмов.");
        return new ArrayList<>(allFilms.values());
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(++filmIdSequence);
        allFilms.put(film.getId(), film);
        log.info(String.format("Фильм %d успешно добавлен.", film.getId()));
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!allFilms.containsKey(film.getId()))
            throw new NotFoundException("Фильм не найден!");
        allFilms.put(film.getId(), film);
        log.info(String.format("Фильм %d успешно изменён.", film.getId()));
        return film;
    }

    @Override
    public Film addLike(Integer id, Integer userId) {
        Film film = allFilms.get(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        film.getLikes().add(userId);
        log.info(String.format("Добавлен лайк фильму %d пользователем %d.", id, userId));
        return film;
    }

    @Override
    public Film deleteLike(Integer id, Integer userId) {
        Film film = allFilms.get(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        film.getLikes().remove(userId);
        log.info(String.format("Добавлен лайк фильму %d пользователем %d.", id, userId));
        return film;
    }

    @Override
    public List<Film> getMostPopular(Integer count) {
        return getAllFilms()
                .stream().sorted(Comparator.comparing(Film::getLikesNumber).reversed())
                .limit(count)
                .collect(Collectors.toList());

    }
}
