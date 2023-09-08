package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    Director add(Director director);

    Director update(Director director);

    void delete(int id);

    Director get(int id);

    List<Director> getAll();

    Boolean exists(int id);
}
