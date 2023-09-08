package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DbReviewStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review addReview(Review review) {
        findFilm(review.getFilmId());
        findUser(review.getUserId());

        String sqlQueryT009 = "INSERT INTO t009_reviews (t009_content, t009_is_positive, t002_id, t001_id) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQueryT009, new String[]{"t009_id"});
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setInt(3, review.getUserId());
            statement.setInt(4, review.getFilmId());
            return statement;
        }, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        review.setUseful(0);
        addToFeedReviewCreate(review.getReviewId(), review.getUserId());
        return getReview(review.getReviewId());
    }

    @Override
    public Review updateReview(Review review) {
        if (getReview(review.getReviewId()) == null) {
            throw new NotFoundException(String.format("Отзыв %d не найден!", review.getReviewId()));
        }

        String sqlQueryT009 = "UPDATE t009_reviews " +
                "SET t009_content = ?, t009_is_positive = ? " +
                "WHERE t009_id = ?";
        jdbcTemplate.update(sqlQueryT009,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        log.info(String.format("Отзыв %d успешно изменён.", review.getReviewId()));
        addToFeedReviewUpdate(review.getReviewId());
        return getReview(review.getReviewId());
    }

    @Override
    public Boolean deleteReview(Integer id) {
        if (getReview(id) == null) {
            throw new NotFoundException(String.format("Отзыв %d не найден!", id));
        }

        String sqlQueryDeleteReviewFeedbacks = "delete from t010_review_feedbacks where t009_id = ?";
        jdbcTemplate.update(sqlQueryDeleteReviewFeedbacks, id);

        String sqlQueryDeleteReview = "delete from t009_reviews where t009_id = ?";
        addToFeedReviewDelete(id, getReview(id).getUserId());
        return jdbcTemplate.update(sqlQueryDeleteReview, id) > 0;
    }

    @Override
    public Review getReview(Integer id) {
        String sqlQueryT009 = "SELECT r.t009_id, r.t009_content, r.t009_is_positive, r.t002_id, " +
                "r.t001_id, rf.t010_value " +
                "FROM t009_reviews r " +
                "LEFT JOIN t010_review_feedbacks rf ON rf.t009_id = r.t009_id " +
                "WHERE r.t009_id = ?";
        List<Review> resultList = jdbcTemplate.query(sqlQueryT009, (rs, rowNum) -> mapRecordToReview(rs), id);
        Review review = resultList.stream().findFirst().orElse(null);
        if (review == null) {
            throw new NotFoundException(String.format("Отзыв %d не найден!", id));
        }
        return review;
    }

    @Override
    public List<Review> getAllReviews(Integer count) {
        String sqlQueryT009 = "SELECT r.t009_id, r.t009_content, r.t009_is_positive, r.t002_id, " +
                "r.t001_id, rf.t010_value " +
                "FROM t009_reviews r " +
                "LEFT JOIN t010_review_feedbacks rf ON rf.t009_id = r.t009_id " +
                "LIMIT ?";
        List<Review> resultList = jdbcTemplate.query(sqlQueryT009, (rs, rowNum) -> mapRecordToReview(rs), count);

        return resultList.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
        String sqlQueryT009 = "SELECT r.t009_id, r.t009_content, r.t009_is_positive, r.t002_id, " +
                "r.t001_id, rf.t010_value " +
                "FROM t009_reviews r " +
                "LEFT JOIN t010_review_feedbacks rf ON rf.t009_id = r.t009_id " +
                "WHERE r.t001_id = ? " +
                "LIMIT ?";
        List<Review> resultList = jdbcTemplate.query(sqlQueryT009, (rs, rowNum) -> mapRecordToReview(rs), filmId, count);

        return resultList.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        insertLikeOrDislikeInTable(id, userId, 1);
    }

    @Override
    public void addDislike(Integer id, Integer userId) {
        insertLikeOrDislikeInTable(id, userId, -1);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        deleteLikeOrDislike(id, userId, 1);
    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {
        deleteLikeOrDislike(id, userId, -1);
    }

    private Review mapRecordToReview(ResultSet rs) {
        try {
            Integer id = rs.getInt("t009_id");
            String content = rs.getString("t009_content");
            Boolean isPositive = rs.getBoolean("t009_is_positive");
            Integer userId = rs.getInt("t002_id");
            Integer filmId = rs.getInt("t001_id");
            Integer useful = rs.getInt("t010_value");
            return new Review(id, content, isPositive, userId, filmId, useful);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи об отзыве! Сообщение: %s", e.getMessage()));
        }
    }

    private void findFilm(Integer filmId) {
        String sqlQueryForFilm = "SELECT t001_id FROM t001_films WHERE t001_id = ?";
        SqlRowSet rowSetFilm = jdbcTemplate.queryForRowSet(sqlQueryForFilm, filmId);
        if (!rowSetFilm.next()) {
            throw new NotFoundException(String.format("Фильм %d не найден!", filmId));
        }
    }

    private void findUser(Integer userId) {
        String sqlQueryForUser = "SELECT t002_id FROM t002_users WHERE t002_id = ?";
        SqlRowSet rowSetUser = jdbcTemplate.queryForRowSet(sqlQueryForUser, userId);
        if (!rowSetUser.next()) {
            throw new NotFoundException(String.format("Пользователь %d не найден!", userId));
        }
    }

    private void findLikeOrDislike(Integer id, Integer userId) {
        String sqlQuery010 = "SELECT t010_value FROM t010_review_feedbacks WHERE t009_id = ? AND t002_id = ?";
        SqlRowSet rowSetUser = jdbcTemplate.queryForRowSet(sqlQuery010, id, userId);
        if (!rowSetUser.next()) {
            throw new NotFoundException(String.format(
                    "Лайк/дизлайк отзыву %d от пользователя %d не найден!",
                    id,
                    userId)
            );
        }
    }

    private void insertLikeOrDislikeInTable(Integer id, Integer userId, Integer value) {
        findUser(userId);
        Review review = getReview(id);

        String sqlQueryIfLikeAlreadyAdded = "SELECT t010_value FROM t010_review_feedbacks WHERE t002_id = ?";
        SqlRowSet rowSetUser = jdbcTemplate.queryForRowSet(sqlQueryIfLikeAlreadyAdded, userId);
        if (!rowSetUser.next()) {
            addLikeDislikeIfNotExist(review, userId, value);
        } else {
            addIfLikeDislikeAlreadyExist(review, userId, value, rowSetUser);
        }
    }

    private void addLikeDislikeIfNotExist(Review review, Integer userId, Integer value) {
        String sqlQuery010 = "INSERT INTO t010_review_feedbacks (t009_id, t002_id, t010_value) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlQuery010, review.getReviewId(), userId, value);
        review.setUseful(review.getUseful() + value);
        log.info(String.format("Добавлена оценка отзыву %d пользователем %d.", review.getReviewId(), userId));
    }

    private void addIfLikeDislikeAlreadyExist(Review review, Integer userId, Integer value, SqlRowSet rowSetUser) {
        if (Objects.equals(rowSetUser.getString(0), value.toString())) {
            throw new NotFoundException(String.format("Пользователь %d уже поставил оценку отзыву!", userId));
        } else {
            String sqlQuery010 = "UPDATE t010_review_feedbacks SET t010_value = ? WHERE t009_id = ? AND t002_id = ?";
            jdbcTemplate.update(sqlQuery010, -value, review.getReviewId(), userId);
            review.setUseful(review.getUseful() + (-value * 2));
            log.info(String.format("Пользователь %d изменил оценку отзыва %d на лайк.", userId, review.getReviewId()));
        }
    }

    private void deleteLikeOrDislike(Integer id, Integer userId, Integer value) {
        findUser(userId);
        findLikeOrDislike(id, userId);
        Review review = getReview(id);

        String sqlQuery010 = "DELETE FROM t010_review_feedbacks WHERE t009_id = ? AND t002_id = ?";
        jdbcTemplate.update(sqlQuery010, id, userId);
        review.setUseful(review.getUseful() - value);
        log.info(String.format("Удален лайк/дизлайк отзыву %d пользователем %d.", id, userId));
    }

    private void addToFeedReviewUpdate(Integer reviewId) {
        String sqlQuery = "INSERT INTO t011_feeds (t002_id, t011_event_type, t011_operation," +
                " t011_entity_id, t011_timestamp) " +
                "VALUES (?, 'REVIEW', 'UPDATE', ?,?)";
        jdbcTemplate.update(sqlQuery, getReview(reviewId).getUserId(),
                reviewId, Date.from(Instant.now()));
    }

    private void addToFeedReviewCreate(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO t011_feeds (t002_id, t011_event_type, t011_operation," +
                " t011_entity_id, t011_timestamp) " +
                "VALUES (?, 'REVIEW', 'ADD', ?,?)";
        jdbcTemplate.update(sql, userId, reviewId, Date.from(Instant.now()));
    }

    private void addToFeedReviewDelete(Integer reviewId, Integer userId) {
        String sqlQuery = "INSERT INTO t011_feeds (t002_id, t011_event_type, t011_operation," +
                " t011_entity_id, t011_timestamp)" +
                " VALUES (?, 'REVIEW', 'REMOVE', ?,?)";
        jdbcTemplate.update(sqlQuery, userId, reviewId, Date.from(Instant.now()));
    }
}