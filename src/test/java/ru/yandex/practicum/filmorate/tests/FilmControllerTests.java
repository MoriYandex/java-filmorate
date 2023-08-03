package ru.yandex.practicum.filmorate.tests;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1985, 12, 28);
    private static final String VERY_LONG_DESCRIPTION = "Очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень-очень длинное описание";
    private static final String DESCRIPTION_200 = VERY_LONG_DESCRIPTION.substring(0, 200);
    FilmController filmController = new FilmController();

    @Test
    void validateFilm() {
        final Film film1 = new Film(0, "  ", "description", LocalDate.of(2000, 1, 1), 100);
        assertThrows(ValidationException.class, () -> filmController.validateFilm(film1));
        final Film film2 = new Film(0, "name", VERY_LONG_DESCRIPTION, LocalDate.of(2000, 1, 1), 100);
        assertThrows(ValidationException.class, () -> filmController.validateFilm(film2));
        final Film film3 = new Film(0, "name", "description", LocalDate.of(1800, 1, 1), 100);
        assertThrows(ValidationException.class, () -> filmController.validateFilm(film3));
        final Film film4 = new Film(0, "name", "description", LocalDate.of(2000, 1, 1), 0);
        assertThrows(ValidationException.class, () -> filmController.validateFilm(film4));
        String strangeName = "  1  ";
        film1.setName(strangeName);
        filmController.validateFilm(film1);
        assertEquals(film1.getName(), strangeName);
        film2.setDescription(DESCRIPTION_200);
        filmController.validateFilm(film2);
        assertEquals(film2.getDescription().length(), 200);
        film3.setReleaseDate(MIN_RELEASE_DATE);
        filmController.validateFilm(film3);
        assertEquals(film3.getReleaseDate(), MIN_RELEASE_DATE);
        film4.setDuration(1);
        filmController.validateFilm(film4);
        assertEquals(film4.getDuration(), 1);
    }
}