package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Integer id){
        log.info("Поиск фильма");
        return filmService.getFilmById(id);
    }

    @GetMapping()
    public List<Film> getAllFilms() {
        log.info("FilmController: вызов списка всех фильмов");
        return filmService.getAllFilms();
    }

    @PostMapping()
    public Film addFilm(@RequestBody Film film) {
        log.info("FilmController: добавление фильма");
        return filmService.addFilm(film);
    }

    @PutMapping()
    public Film updateFilm(@RequestBody Film film) {
        log.info("FilmController: изменение фильма");
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Integer id, @PathVariable Integer userId){
        log.info("FilmController: добавление лайка");
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Integer id, @PathVariable Integer userId){
        log.info("FilmController: удаление лайка");
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopular (@RequestParam(required = false) Integer count){
        log.info(String.format("FilmController: поиск наиболее популярных %sфильмов", (count != null && count > 0 ) ? count + " " : ""));
        return filmService.getMostPopular(count);
    }
}
