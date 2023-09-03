package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenreToFilm {
    private Integer id;
    private Integer genreId;
    private Integer filmId;
}
