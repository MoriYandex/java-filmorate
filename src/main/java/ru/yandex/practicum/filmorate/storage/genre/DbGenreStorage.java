package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Qualifier("DbGenreStorage")
@Slf4j
@RequiredArgsConstructor
public class DbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getGenre(Integer id) {
        String query = "SELECT * FROM t005_genres WHERE t005_id = ?";
        List<Genre> resultList = jdbcTemplate.query(query, (rs, rowNum) -> mapGenre(rs), id);
        return resultList.stream().findFirst().orElse(null);
    }

    @Override
    public List<Genre> getAllGenres() {
        String query = "SELECT * FROM t005_genres";
        return jdbcTemplate.query(query, (rs, rowNum) -> mapGenre(rs));
    }

    @Override
    public List<Genre> getAllGenresByFilmId(Integer filmId) {
        String query = "SELECT t005.* FROM t005_genres t005 JOIN t007_links_t001_t005 t007 on t007.t005_id = t005.t005_id WHERE t007.t001_id = ?";
        return jdbcTemplate.query(query, (rs, rowNum) -> mapGenre(rs), filmId);
    }

    private Genre mapGenre(ResultSet rs) {
        try {
            Integer id = rs.getInt("t005_id");
            String name = rs.getString("t005_name");
            return new Genre(id, name);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о жанре! Сообщение: %s", e.getMessage()));
        }
    }
}
