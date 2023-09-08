package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director addDirector(Director director) {
        if (StringUtils.isBlank(director.getName())) {
            throw new ValidationException("Имя режиссера не должно быть пустым");
        }
        return directorStorage.add(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.update(director);
    }

    public void deleteDirector(int id) {
        directorStorage.delete(id);
    }

    public Director getDirector(int id) {
        Director director = directorStorage.get(id);
        if (director == null) {
            throw new NotFoundException(String.format("Режиссер с id %s не найден", id));
        }
        return director;
    }

    public List<Director> getAllDirectors() {
        return directorStorage.getAll();
    }
}
