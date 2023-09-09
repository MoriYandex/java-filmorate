package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {
    private final RatingStorage ratingStorage;

    public Rating getRating(Integer id) {
        log.info("Поиск рейтинга по идентификатору {}", id);
        Rating rating = ratingStorage.getRating(id);
        if (rating == null)
            throw new NotFoundException(String.format("Рейтинг %d не найден!", id));
        return rating;
    }

    public List<Rating> getAllRatings() {
        log.info("Получение списка всех рейтингов");
        return ratingStorage.getAllRatings();
    }
}
