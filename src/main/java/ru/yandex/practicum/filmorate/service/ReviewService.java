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
        log.info("ReviewService: Добавление отзыва");
        return reviewStorage.add(review);
    }

    public Review updateReview(Review review) {
        log.info(String.format("ReviewService: Изменение данных отзыва по идентификатору %d", review.getReviewId()));
        return reviewStorage.update(review);
    }

    public Boolean deleteReview(Integer id) {
        log.info(String.format("ReviewService: Удаление отзыва по идентификатору %d", id));
        return reviewStorage.delete(id);
    }

    public Review getReview(Integer id) {
        log.info(String.format("ReviewService: Поиск отзыва по идентификатору %d", id));
        return reviewStorage.get(id);
    }

    public List<Review> getAllReviews(Integer filmId, Integer count) {
        if (filmId == null) {
            log.info(String.format("ReviewService: Получение списка %d отзывов", count));
            return reviewStorage.getAll(count);
        } else {
            log.info(String.format("ReviewService: Получение списка %d отзывов для фильма с id %d.", count, filmId));
            return reviewStorage.getAllByFilmId(filmId, count);
        }
    }

    public void addLike(Integer id, Integer userId) {
        log.info(String.format("ReviewService: Добавление лайка отзыву %d пользователем %d", id, userId));
        reviewStorage.addLike(id, userId);
    }

    public void addDislike(Integer id, Integer userId) {
        log.info(String.format("ReviewService: Добавление дизлайка отзыву %d пользователем %d", id, userId));
        reviewStorage.addDislike(id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        log.info(String.format("ReviewService: Удаление лайка отзыву %d пользователем %d", id, userId));
        reviewStorage.deleteLike(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        log.info(String.format("ReviewService: Удаление дизлайка отзыву %d пользователем %d", id, userId));
        reviewStorage.deleteDislike(id, userId);
    }
}