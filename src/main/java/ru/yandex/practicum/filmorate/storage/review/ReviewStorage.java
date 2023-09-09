package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review add(Review review);

    Review update(Review review);

    Boolean delete(Integer id);

    Review get(Integer id);

    List<Review> getAll(Integer count);

    List<Review> getAllByFilmId(Integer filmId, Integer count);

    void addLike(Integer id, Integer userId);

    void addDislike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    void deleteDislike(Integer id, Integer userId);
}