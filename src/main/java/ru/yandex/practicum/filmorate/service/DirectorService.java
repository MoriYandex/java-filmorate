package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director createDirector(Director director) {
        if (director.getName().isBlank()) {
            throw new ValidationException("Имя режиссера не дожно быть пустым");
        }
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(int id) {
        directorStorage.deleteDirector(id);
    }

    public Director getDirectorById(int id) {
        Director director = directorStorage.getDirectorById(id);
        if (director == null) {
            throw new NotFoundException(String.format("Режиссер с id %s не найден", id));
        }
        return director;
    }

    public List<Director> getListAllDirectors() {
        return directorStorage.getListAllDirectors();
    }

    public void load(List<Film> film) {
        directorStorage.load(film);
    }

}
