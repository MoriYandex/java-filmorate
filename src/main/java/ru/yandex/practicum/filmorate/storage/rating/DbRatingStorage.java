package ru.yandex.practicum.filmorate.storage.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Qualifier("DbRatingStorage")
@Slf4j
@RequiredArgsConstructor
public class DbRatingStorage implements RatingStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Rating getRating(Integer id) {
        String query = "SELECT * FROM t006_ratings WHERE t006_id = ?";
        List<Rating> resultList = jdbcTemplate.query(query, (rs, rowNum) -> mapRating(rs), id);
        return resultList.stream().findFirst().orElse(null);
    }

    @Override
    public List<Rating> getAllRatings() {
        String query = "SELECT * FROM t006_ratings";
        return jdbcTemplate.query(query, (rs, rowNum) -> mapRating(rs));
    }

    private Rating mapRating(ResultSet rs) {
        try {
            Integer id = rs.getInt("t006_id");
            String name = rs.getString("t006_code");
            String description = rs.getString("t006_description");
            return new Rating(id, name, description);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о рейтинге! Сообщение: %s", e.getMessage()));
        }
    }
}
