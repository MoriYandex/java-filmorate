package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component("DbFilmStorage")
@Slf4j
@RequiredArgsConstructor
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;

    @Override
    public Film get(Integer id) {
        String query = "SELECT * FROM t001_films t001 LEFT JOIN t006_ratings t006 ON t006.t006_id = t001.t006_id " +
                "LEFT JOIN t008_directors t008 ON t008.t008_id = T001.T008_ID WHERE t001.t001_id = ?";
        List<Film> resultList = jdbcTemplate.query(query, (rs, rowNum) -> mapRecordToFilm(rs), id);
        Film film = resultList.stream().findFirst().orElse(null);
        if (film == null)
            return null;
        film.setGenres(genreStorage.getAllByFilmId(id));
        film.setLikes(getLikesByFilmId(id));
        return film;
    }

    @Override
    public List<Film> getAll() {
        String query = "SELECT * FROM t001_films t001 LEFT JOIN t006_ratings t006 ON t006.t006_id = t001.t006_id " +
                "LEFT JOIN t008_directors t008 ON t008.t008_id = T001.T008_ID";
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
    public List<Film> getAllByDirId(Integer dirId) {
        String query = "SELECT * FROM t001_films t001 LEFT JOIN t006_ratings t006 ON t006.t006_id = t001.t006_id " +
                "LEFT JOIN t008_directors t008 ON t008.t008_id = T001.T008_ID " +
                "WHERE T001.T008_ID = ?";
        List<Film> resultList = jdbcTemplate.query(query, (rs, rowNum) -> mapRecordToFilm(rs), dirId);
        Map<Integer, List<Genre>> mapGenresToFilms = genreStorage.getMapOfGenresToFilmsByDirId(dirId);
        List<Like> likeList = getAllLikesByDirId(dirId);
        for (Film film : resultList) {
            List<Genre> genreList = mapGenresToFilms.get(film.getId());
            film.setGenres(genreList != null ? genreList : new ArrayList<>());
            film.setLikes(likeList.stream().filter(x -> film.getId().equals(x.getFilmId())).map(Like::getUserId).collect(Collectors.toSet()));
        }
        return resultList;
    }

    @Override
    public List<Film> search(String query, String by) {
        List<Film> resultList = new ArrayList<>();
        String sqlQuery = "SELECT * FROM t001_films t001 LEFT JOIN t006_ratings t006 ON t006.t006_id = t001.t006_id " +
                "LEFT JOIN t008_directors t008 ON t008.t008_id = T001.T008_ID";
        if (by.contains("director") && by.contains("title")) {
            sqlQuery = sqlQuery + " WHERE lower(t008.t008_name) like ('%' || ? || '%') OR lower(t001.t001_name) like ('%' || ? || '%')";
            resultList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRecordToFilm(rs), query.toLowerCase(), query.toLowerCase());
        } else if (by.contains("director")) {
            sqlQuery = sqlQuery + " WHERE lower(t008.t008_name) like ('%' || ? || '%')";
            resultList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRecordToFilm(rs), query.toLowerCase());
        } else if (by.contains("title")) {
            sqlQuery = sqlQuery + " WHERE lower(t001.t001_name) like ('%' || ? || '%')";
            resultList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRecordToFilm(rs), query.toLowerCase());
        }
        if (resultList.isEmpty())
            return resultList;
        List<Integer> filmIds = resultList.stream().map(Film::getId).collect(Collectors.toList());
        Map<Integer, List<Genre>> mapGenresToFilms = genreStorage.getMapOfGenresToFilmsByFilmIds(filmIds);
        List<Like> likeList = getAllLikesByFilmIds(filmIds);
        for (Film film : resultList) {
            List<Genre> genreList = mapGenresToFilms.get(film.getId());
            film.setGenres(genreList != null ? genreList : new ArrayList<>());
            film.setLikes(likeList.stream().filter(x -> film.getId().equals(x.getFilmId())).map(Like::getUserId).collect(Collectors.toSet()));
        }
        return resultList;
    }

    @Override
    public Film add(Film film) {
        String sqlQueryT001 = "INSERT INTO t001_films (t001_name, t001_description, t001_release_date, t001_duration, t006_id, t008_id) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlQueryT007 = "INSERT INTO t007_links_t001_t005 (t001_id, t005_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQueryT001, new String[]{"t001_id"});
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            statement.setObject(6, film.getDirectors().isEmpty() ? null : film.getDirectors().get(0).getId(), Types.INTEGER);
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
    public Film update(Film film) {
        if (get(film.getId()) == null) {
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
                film.getDirectors().isEmpty() ? null : film.getDirectors().get(0).getId(),
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
        Film film = get(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        addToFeedAddLike(userId, id);
        if (film.getLikes().contains(userId))
            return film;
        String sqlQuery003 = "INSERT INTO t003_likes (t001_id, t002_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery003, id, userId);
        film.getLikes().add(userId);
        log.info("Добавлен лайк фильму {} пользователем {}.", id, userId);
        return film;
    }

    @Override
    public Film deleteLike(Integer id, Integer userId) {
        Film film = get(id);
        if (film == null)
            throw new NotFoundException(String.format("Фильм %d не найден!", id));
        addToFeedDeleteLike(userId, id);
        if (!film.getLikes().contains(userId))
            return film;
        String sqlQuery003 = "DELETE FROM t003_likes WHERE t001_id = ? AND t002_id = ?";
        jdbcTemplate.update(sqlQuery003, id, userId);
        film.getLikes().remove(userId);
        log.info("Удалён лайк фильму {} пользователем {}.", id, userId);
        return film;
    }

    @Override
    public List<Film> getMostPopular(Integer count, Integer genreId, Integer year) {
        String sqlQuery = setQueryWithCount();

        MapSqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("count", count);
        if (genreId == null && year != null) {
            sqlQuery = changeQueryIfYearExists();
            paramSource.addValue("year", year);
        }

        if (genreId != null && year == null) {
            sqlQuery = changeQueryIfGenreExists();
            paramSource.addValue("genreId", genreId);
        }

        if (genreId != null && year != null) {
            sqlQuery = changeQueryIfGenreAndYearExist();
            paramSource.addValue("year", year);
            paramSource.addValue("genreId", genreId);
        }

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        List<Film> resultList = namedParameterJdbcTemplate.query(sqlQuery, paramSource, (rs, rowNum) -> mapRecordToFilm(rs));
        List<Integer> filmIds = resultList.stream().map(Film::getId).collect(Collectors.toList());
        Map<Integer, List<Genre>> mapGenresToFilms = genreStorage.getMapOfGenresToFilmsByFilmIds(filmIds);
        List<Like> likeList = getAllLikesByFilmIds(filmIds);
        for (Film film : resultList) {
            List<Genre> genreList = mapGenresToFilms.get(film.getId());
            film.setGenres(genreList != null ? genreList : new ArrayList<>());
            film.setLikes(likeList.stream().filter(x -> film.getId().equals(x.getFilmId())).map(Like::getUserId).collect(Collectors.toSet()));
        }
        return resultList;
    }

    @Override
    public Film delete(Integer id) {
        Film film = get(id);
        if (film == null) {
            throw new NotFoundException(String.format("Фильма с id %d не существует", id));
        }
        String query = "DELETE FROM t001_films WHERE t001_id = ?";
        jdbcTemplate.update(query, id);
        log.info("Фильм {} был успешно удалён!", film);
        return film;
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sqlCommonFilms = "SELECT f.t001_id FROM t003_likes AS l1 " +
                "LEFT JOIN t001_films AS f ON f.t001_id = l1.t001_id " +
                "LEFT JOIN t003_likes AS l2 ON l1.t001_id = l2.t001_id " +
                "WHERE l1.t002_id = ? AND l2.t002_id = ? AND l1.t001_id = l2.t001_id";
        List<Film> resultList = jdbcTemplate.query(
                sqlCommonFilms,
                (rs, rowNum) -> get(rs.getInt("t001_id")),
                userId,
                friendId
        );

        return resultList.stream()
                .sorted(Comparator.comparing(Film::getLikesCount).reversed())
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
            int directorId = rs.getInt("t008_id");
            String directorName = rs.getString("t008_name");
            List<Director> directors = new ArrayList<>();
            if (directorId != 0) {
                Director director = Director.builder()
                        .id(directorId)
                        .name(directorName)
                        .build();
                directors.add(director);
            }
            return new Film(id, name, description, releaseDate, duration, new ArrayList<>(), new Rating(ratingId,
                    ratingName, ratingDescription), new HashSet<>(), directors);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о фильме! Сообщение: %s", e.getMessage()));
        }
    }

    private String setQueryWithCount() {
        return "SELECT f.t001_id, f.t001_name, f.t001_description, f.t001_release_date,  f.t001_duration, " +
                "f.t006_id, r.t006_code, r.t006_description, f.t008_id, d.t008_name " +
                "FROM t001_films f " +
                "LEFT JOIN t006_ratings r ON f.t006_id = r.t006_id " +
                "LEFT JOIN t003_likes l on f.t001_id  = l.t001_id " +
                "LEFT JOIN t008_directors d ON f.t008_id = d.t008_id " +
                "GROUP BY f.t001_id " +
                "ORDER BY COUNT(l.t002_id) DESC " +
                "LIMIT :count";
    }

    private String changeQueryIfGenreExists() {
        return "SELECT f.t001_id, f.t001_name, f.t001_description, f.t001_release_date,  f.t001_duration, " +
                "f.t006_id, r.t006_code, r.t006_description, g.t005_id, f.t008_id, d.t008_name " +
                "FROM t001_films f " +
                "LEFT JOIN t006_ratings r ON f.t006_id = r.t006_id " +
                "LEFT JOIN t003_likes l on f.t001_id  = l.t001_id " +
                "LEFT JOIN t007_links_t001_t005 g on f.t001_id = g.t001_id " +
                "LEFT JOIN t008_directors d ON f.t008_id = d.t008_id " +
                "WHERE g.t005_id = :genreId " +
                "GROUP BY f.t001_id, l.t002_id " +
                "ORDER BY COUNT(l.t002_id) DESC " +
                "LIMIT :count";
    }

    private String changeQueryIfYearExists() {
        return "SELECT f.t001_id, f.t001_name, f.t001_description, f.t001_release_date,  f.t001_duration, " +
                "f.t006_id, r.t006_code, r.t006_description, f.t008_id, d.t008_name  " +
                "FROM t001_films f " +
                "LEFT JOIN t006_ratings r ON f.t006_id = r.t006_id " +
                "LEFT JOIN t003_likes l on f.t001_id  = l.t001_id " +
                "LEFT JOIN t008_directors d ON f.t008_id = d.t008_id " +
                "WHERE EXTRACT(YEAR FROM t001_release_date) = :year " +
                "GROUP BY f.t001_id, l.t002_id " +
                "ORDER BY COUNT(l.t002_id) DESC " +
                "LIMIT :count";
    }

    private String changeQueryIfGenreAndYearExist() {
        return "SELECT f.t001_id, f.t001_name, f.t001_description, f.t001_release_date,  f.t001_duration, " +
                "f.t006_id, r.t006_code, r.t006_description, g.t005_id, f.t008_id, d.t008_name   " +
                "FROM t001_films f " +
                "LEFT JOIN t006_ratings r ON f.t006_id = r.t006_id " +
                "LEFT JOIN t003_likes l on f.t001_id  = l.t001_id " +
                "LEFT JOIN t007_links_t001_t005 g on f.t001_id = g.t001_id " +
                "LEFT JOIN t008_directors d ON f.t008_id = d.t008_id " +
                "WHERE g.t005_id = :genreId AND EXTRACT(YEAR FROM t001_release_date) = :year " +
                "GROUP BY f.t001_id, l.t002_id " +
                "ORDER BY COUNT(l.t002_id) DESC " +
                "LIMIT :count";
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

    private List<Like> getAllLikesByDirId(Integer dirId) {
        String sqlQueryT003 = "SELECT * FROM t003_likes t003" +
                " JOIN t001_films t001 on t001.t001_id = t003.t001_id" +
                " WHERE t001.t008_id = ?";
        return jdbcTemplate.query(sqlQueryT003, (rs, rowNum) -> mapRecordToLike(rs), dirId);
    }

    private List<Like> getAllLikesByFilmIds(Collection<Integer> filmIds) {
        jdbcTemplate.execute("CREATE TEMPORARY TABLE IF NOT EXISTS films_tmp (id INT NOT NULL)");
        List<Object[]> objFilmIds = new ArrayList<>();
        for (Integer id : filmIds) {
            objFilmIds.add(new Object[]{id});
        }
        jdbcTemplate.batchUpdate("INSERT INTO films_tmp VALUES(?)", objFilmIds);
        String sqlQueryT003 = "SELECT * FROM t003_likes t003" +
                " JOIN t001_films t001 on t001.t001_id = t003.t001_id" +
                " WHERE t001.t001_id IN (SELECT id FROM films_tmp)";
        List<Like> resultList = jdbcTemplate.query(sqlQueryT003, (rs, rowNum) -> mapRecordToLike(rs));
        jdbcTemplate.update("DELETE FROM films_tmp");
        return resultList;
    }

    private void addToFeedAddLike(Integer userId, Integer filmId) {
        String sql = "INSERT INTO t011_feeds (t002_id, t011_event_type, t011_operation," +
                " t011_entity_id, t011_timestamp)" +
                " VALUES (?, 'LIKE', 'ADD', ?, ?)";
        jdbcTemplate.update(sql, userId, filmId, java.sql.Date.from(Instant.now()));
    }

    private void addToFeedDeleteLike(Integer userId, Integer filmId) {
        String sql = "INSERT INTO t011_feeds (t002_id, t011_event_type, t011_operation," +
                " t011_entity_id, t011_timestamp)" +
                " VALUES (?, 'LIKE', 'REMOVE', ?, ?)";
        jdbcTemplate.update(sql, userId, filmId, java.sql.Date.from(Instant.now()));
    }

    @Override
    public List<Film> getRecommendFilms(Integer userId) {
        String sqlQuery = "WITH intersections as (SELECT t002_id as t002_id, count (t001_id) as intercount" +
                "                                   FROM t003_likes" +
                "                                   WHERE t001_id in (SELECT t001_id FROM t003_likes WHERE t002_id = ? )" +
                "                                       AND t002_id <> ?  " +
                "                                       GROUP BY t002_id)" +
                " SELECT * FROM t001_films t001 LEFT JOIN t006_ratings t006 ON t006.t006_id = t001.t006_id " +
                "   LEFT JOIN t008_directors t008 ON t008.t008_id = T001.T008_ID" +
                " WHERE t001.t001_id IN (SELECT  DISTINCT t003.t001_id as t001_id" +
                "                FROM t003_likes t003" +
                "                JOIN intersections i ON i.t002_id = t003.t002_id" +
                "                WHERE i.intercount = (SELECT MAX(intercount) FROM intersections)" +
                "                AND NOT (t003.t001_id IN (SELECT t001_id FROM (SELECT t001_id FROM t003_likes WHERE t002_id = ? ))))";
        List<Film> resultList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRecordToFilm(rs), userId, userId, userId);
        if (resultList.isEmpty())
            return resultList;
        List<Integer> filmIds = resultList.stream().map(Film::getId).collect(Collectors.toList());
        Map<Integer, List<Genre>> mapGenresToFilms = genreStorage.getMapOfGenresToFilmsByFilmIds(filmIds);
        List<Like> likeList = getAllLikesByFilmIds(filmIds);
        for (Film film : resultList) {
            List<Genre> genreList = mapGenresToFilms.get(film.getId());
            film.setGenres(genreList != null ? genreList : new ArrayList<>());
            film.setLikes(likeList.stream().filter(x -> film.getId().equals(x.getFilmId())).map(Like::getUserId).collect(Collectors.toSet()));
        }
        return resultList;
    }
}
