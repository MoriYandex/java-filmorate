package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private List<Genre> genres = new ArrayList<>();
    private Rating mpa = new Rating();
    private Set<Integer> likes = new HashSet<>();

    public int getLikesCount() {
        return likes.size();
    }
}
