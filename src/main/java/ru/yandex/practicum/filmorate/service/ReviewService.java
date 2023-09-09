package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public Review addReview(Review review) {
        log.info("Добавление отзыва");
        return reviewStorage.add(review);
    }

    public Review updateReview(Review review) {
        log.info("Изменение данных отзыва по идентификатору {}", review.getReviewId());
        return reviewStorage.update(review);
    }

    public Boolean deleteReview(Integer id) {
        log.info("Удаление отзыва по идентификатору {}", id);
        return reviewStorage.delete(id);
    }

    public Review getReview(Integer id) {
        log.info("Поиск отзыва по идентификатору {}", id);
        return reviewStorage.get(id);
    }

    public List<Review> getAllReviews(Integer filmId, Integer count) {
        if (filmId == null) {
            log.info("Получение списка {} отзывов", count);
            return reviewStorage.getAll(count);
        } else {
            log.info("Получение списка {} отзывов для фильма с id {}.", count, filmId);
            return reviewStorage.getAllByFilmId(filmId, count);
        }
    }

    public void addLike(Integer id, Integer userId) {
        log.info("Добавление лайка отзыву {} пользователем {}", id, userId);
        reviewStorage.addLike(id, userId);
    }

    public void addDislike(Integer id, Integer userId) {
        log.info("Добавление дизлайка отзыву {} пользователем {}", id, userId);
        reviewStorage.addDislike(id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        log.info("Удаление лайка отзыву {} пользователем {}", id, userId);
        reviewStorage.deleteLike(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        log.info("Удаление дизлайка отзыву {} пользователем {}", id, userId);
        reviewStorage.deleteDislike(id, userId);
    }
}