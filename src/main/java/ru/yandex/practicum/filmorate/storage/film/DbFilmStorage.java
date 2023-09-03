package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component("DbFilmStorage")
@Slf4j
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final GenreStorage genreStorage;

    public DbFilmStorage(JdbcTemplate jdbcTemplate, @Qualifier("DbGenreStorage") GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
    }

    @Override
    public Film getFilm(Integer id) {
        String query = "SELECT * FROM t001_films t001 LEFT JOIN t006_ratings t006 ON t006.t006_id = t001.t006_id WHERE t001.t001_id = ?";
        List<Film> resultList = jdbcTemplate.query(query, (rs, rowNum) -> mapRecordToFilm(rs), id);
        Film film = resultList.stream().findFirst().orElse(null);
        if (film == null)
            return null;
        film.setGenres(genreStorage.getAllGenresByFilmId(id));
        film.setLikes(getLikesByFilmId(id));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String query = "SELECT * FROM t001_films t001 LEFT JOIN t006_ratings t006 ON t006.t006_id = t001.t006_id";
        List<Film> resultList = jdbcTemplate.query(query, (rs, rowNum) -> mapRecordToFilm(rs));
        Map<Integer, List<Genre>> mapGenresToFilms = genreStorage.getMapOfGenresToFilms();
        List<Like> likeList = getAllLikes();
        for (Film film : resultList) {
            List<Genre> genreList = mapGenresToFilms.get(film.getId());
            film.setGenres(genreList != null ? genreList : new ArrayList<>());
            film.setLikes(likeList.stream().filter(x -> film.getId().equals(x.getFilmId())).map(Like::getUserId).collect(Collectors.toSet()));
        }
        return resultList;
    }

    @Override
    public Film addFilm(Film film) {
        String sqlQueryT001 = "INSERT INTO t001_films (t001_name, t001_description, t001_release_date, t001_duration, t006_id,) VALUES (?, ?, ?, ?, ?)";
        String sqlQueryT007 = "INSERT INTO t007_links_t001_t005 (t001_id, t005_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQueryT001, new String[]{"t001_id"});
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            return statement;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        film.setGenres(film.getGenres().stream().distinct().collect(Collectors.toList()));
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sqlQueryT007, film.getId(), genre.getId());
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (getFilm(film.getId()) == null) {
            throw new NotFoundException(String.format("Фильм %d не найден!", film.getId()));
        }
        String sqlQueryT001 = "UPDATE t001_films SET t001_name = ?, t001_description = ?, t001_release_date = ?, t001_duration = ?, t006_id = ?, t008_id = ? WHERE t001_id = ?";
        String sqlQueryT007Clear = "DELETE FROM t007_links_t001_t005 WHERE t001_id = ?";
        String sqlQueryT007Insert = "INSERT INTO t007_links_t001_t005 (t001_id, t005_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQueryT001,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        jdbcTemplate.update(sqlQueryT007Clear, film.getId());
        film.setGenres(film.getGenres().stream().distinct().collect(Collectors.toList()));
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sqlQueryT007Insert, film.getId(), genre.getId());
        }
        return film;
    }

    @Override
    public Film addLike(Integer id, Integer userId) {
        Film film = getFilm(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        if (film.getLikes().contains(userId))
            return film;
        String sqlQuery003 = "INSERT INTO t003_likes (t001_id, t002_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery003, id, userId);
        film.getLikes().add(userId);
        log.info(String.format("Добавлен лайк фильму %d пользователем %d.", id, userId));
        return film;
    }

    @Override
    public Film deleteLike(Integer id, Integer userId) {
        Film film = getFilm(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        if (!film.getLikes().contains(userId))
            return film;
        String sqlQuery003 = "DELETE FROM t003_likes WHERE t001_id = ? AND t002_id = ?";
        jdbcTemplate.update(sqlQuery003, id, userId);
        film.getLikes().remove(userId);
        log.info(String.format("Удалён лайк фильму %d пользователем %d.", id, userId));
        return film;
    }

    @Override
    public List<Film> getMostPopular(Integer count) {
        return getAllFilms()
                .stream().sorted(Comparator.comparing(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film mapRecordToFilm(ResultSet rs) {
        try {
            Integer id = rs.getInt("t001_id");
            String name = rs.getString("t001_name");
            String description = rs.getString("t001_description");
            LocalDate releaseDate = rs.getDate("t001_release_date").toLocalDate();
            Integer duration = rs.getInt("t001_duration");
            Integer ratingId = rs.getInt("t006_id");
            String ratingName = rs.getString("t006_code");
            String ratingDescription = rs.getString("t006_description");
            return new Film(id, name, description, releaseDate, duration, new ArrayList<>(),
                    new Rating(ratingId, ratingName, ratingDescription), new HashSet<>(), new HashSet<>());
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о фильме! Сообщение: %s", e.getMessage()));
        }
    }

    private Set<Integer> getLikesByFilmId(Integer id) {
        String sqlQueryT003 = "SELECT t002_id FROM t003_likes WHERE t001_id = ?";
        List<Integer> resultList = jdbcTemplate.query(sqlQueryT003, (rs, rowNum) -> rs.getInt("t002_id"), id);
        return new HashSet<>(resultList);
    }

    private Like mapRecordToLike(ResultSet rs) {
        try {
            Integer id = rs.getInt("t003_id");
            Integer filmId = rs.getInt("t001_id");
            Integer userId = rs.getInt("t002_id");
            return new Like(id, filmId, userId);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о лайке! Сообщение: %s", e.getMessage()));
        }
    }

    private List<Like> getAllLikes() {
        String sqlQueryT003 = "SELECT * FROM t003_likes";
        return jdbcTemplate.query(sqlQueryT003, (rs, rowNum) -> mapRecordToLike(rs));
    }

    @Override
    public List<Film> findFilmsByDirector(String query) {
        String sqlQuery = "SELECT t001_films.t001_id, t001_films.t001_name, t001_films.t001_description, t001_films.t001_release_date, t001_films.t001_duration, t001_films.t006_id, " +
                "t006_ratings.t006_code, t006_ratings.t006_description, t008_directors.t008_id, t008_directors.t008_name " +
                "FROM t001_films " +
                "LEFT JOIN t003_likes ON t001_films.t001_id = t003_likes.t001_id " +
                "LEFT JOIN t007_links_t001_t008 ON t001_films.t001_id = t007_links_t001_t008.t001_id " +
                "LEFT JOIN t008_directors ON t007_links_t001_t008.t008_id = t008_directors.t008_id " +
                "JOIN t006_ratings ON t001_films.t006_id = t006_ratings.t006_id " +
                "WHERE LOWER(t008_directors.t008_name) LIKE ? " +
                "GROUP BY t001_films.t001_id " +
                "ORDER BY COUNT(t003_likes.t001_id) DESC";
        return Collections.singletonList(jdbcTemplate.query(sqlQuery, this::mapRecordToFilm, "%" + query + "%"));
    }

    @Override
    public List<Film> findFilmsByDirectorTitle(String query) {
        String sqlQuery = "SELECT t001_films.t001_id, t001_films.t001_name, t001_films.t001_description, t001_films.t001_release_date, t001_films.t001_duration, t001_films.t006_id, " +
                "t006_ratings.t006_code, t006_ratings.t006_description, t008_directors.t008_id, t008_directors.t008_name " +
                "FROM t001_films " +
                "LEFT JOIN t003_likes ON t001_films.t001_id = t003_likes.t001_id " +
                "LEFT JOIN t007_links_t001_t008 ON t001_films.t001_id = t007_links_t001_t008.t001_id " +
                "LEFT JOIN t008_directors ON t007_links_t001_t008.t008_id = t008_directors.t008_id " +
                "JOIN t006_ratings ON t001_films.t006_id = t006_ratings.t006_id " +
                "WHERE LOWER(t001_films.t001_name) LIKE ? OR LOWER(t008_directors.t008_name) LIKE ? " +
                "GROUP BY t001_films.t001_id " +
                "ORDER BY COUNT(t003_likes.t001_id) DESC";
        return Collections.singletonList(jdbcTemplate.query(sqlQuery, this::mapRecordToFilm, "%" + query + "%", "%" + query + "%"));
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        String sqlQuery = "SELECT t001_films.t001_id, t001_films.t001_name, t001_films.t001_description, t001_films.t001_release_date, t001_films.t001_duration, " +
                "t001_films.t006_id, t006_ratings.t006_code, t006_ratings.t006_description " +
                "FROM t001_films " +
                "LEFT JOIN t003_likes ON t001_films.t001_id = t003_likes.t001_id " +
                "LEFT JOIN t007_links_t001_t008 ON t001_films.t001_id = t007_links_t001_t008.t001_id " +
                "JOIN t006_ratings ON t001_films.t006_id = t006_ratings.t006_id " +
                "WHERE t007_links_t001_t008.t008_id = ? " +
                "GROUP BY t001_films.t001_id " +
                "ORDER BY COUNT(t003_likes.t001_id) DESC";
        return Collections.singletonList(jdbcTemplate.query(sqlQuery, this::mapRecordToFilm, directorId));
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByYears(int directorId) {
        String sqlQuery = "SELECT t001_films.t001_id, t001_films.t001_name, t001_films.t001_description, t001_films.t001_release_date, t001_films.t001_duration, " +
                "t001_films.t006_id, t006_ratings.t006_code, t006_ratings.t006_description " +
                "FROM t001_films " +
                "JOIN t007_links_t001_t008 ON t001_films.t001_id = t007_links_t001_t008.t001_id " +
                "JOIN t006_ratings ON t001_films.t006_id = t006_ratings.t006_id " +
                "WHERE t007_links_t001_t008.t008_id = ? " +
                "ORDER BY t001_films.t001_release_date";
        return Collections.singletonList(jdbcTemplate.query(sqlQuery, this::mapRecordToFilm, directorId));
    }

    @Override
    public void addDirectorToFilm(int filmId, int directorId) {
        String sqlQuery = "INSERT INTO t007_links_t001_t008 (t001_id, t008_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, directorId);
    }
}
