package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;

import java.util.List;

@Service
@Slf4j
public class RatingService {
    private final RatingStorage ratingStorage;

    public RatingService(@Qualifier("DbRatingStorage")
                         RatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Rating getRatingById(Integer id) {
        log.info(String.format("RatingService: Поиск рейтинга по идентификатору %d", id));
        Rating rating = ratingStorage.getRating(id);
        if (rating == null)
            throw new NotFoundException(String.format("Рейтинг %d не найден!", id));
        return rating;
    }

    public List<Rating> getAllRatings() {
        log.info("RatingService: Получение списка всех рейтингов");
        return ratingStorage.getAllRatings();
    }
}
