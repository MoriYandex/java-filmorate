package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    Boolean deleteReview(Integer id);

    Review getReview(Integer id);

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsByFilmId(Integer filmId, Integer count);

    void addLike(Integer id, Integer userId);

    void addDislike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    void deleteDislike(Integer id, Integer userId);
}