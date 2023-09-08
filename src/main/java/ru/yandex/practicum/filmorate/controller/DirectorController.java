package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director get(@PathVariable @Positive int id) {
        return directorService.getDirector(id);
    }

    @PostMapping
    public Director add(@Valid @RequestBody Director director) {
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive int id) {
        directorService.deleteDirector(id);
    }
}
