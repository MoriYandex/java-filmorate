package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreToFilm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Qualifier("DbGenreStorage")
@Slf4j
@RequiredArgsConstructor
public class DbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre get(Integer id) {
        String query = "SELECT * FROM t005_genres WHERE t005_id = ?";
        List<Genre> resultList = jdbcTemplate.query(query, (rs, rowNum) -> mapRecordToGenre(rs), id);
        return resultList.stream().findFirst().orElse(null);
    }

    @Override
    public List<Genre> getAll() {
        String sqlQueryT005 = "SELECT * FROM t005_genres";
        return jdbcTemplate.query(sqlQueryT005, (rs, rowNum) -> mapRecordToGenre(rs));
    }

    private List<GenreToFilm> getAllGenresToFilm() {
        String sqlQueryT007 = "SELECT * FROM t007_links_t001_t005";
        return jdbcTemplate.query(sqlQueryT007, (rs, rowNum) -> mapRecordToGenreToFilm(rs));
    }

    private List<GenreToFilm> getAllGenresToFilmByDirId(Integer dirId) {
        String sqlQueryT007 = "SELECT * FROM t007_links_t001_t005 t007" +
                " JOIN t001_films t001 on t001.t001_id = t007.t001_id" +
                " WHERE t001.t008_id = ?";
        return jdbcTemplate.query(sqlQueryT007, (rs, rowNum) -> mapRecordToGenreToFilm(rs), dirId);
    }

    private List<GenreToFilm> getAllGenresToFilmByFilmIds(Collection<Integer> filmIds) {
        jdbcTemplate.execute("CREATE TEMPORARY TABLE IF NOT EXISTS films_tmp (id INT NOT NULL)");
        List<Object[]> objFilmIds = new ArrayList<>();
        for (Integer id : filmIds) {
            objFilmIds.add(new Object[]{id});
        }
        jdbcTemplate.batchUpdate("INSERT INTO films_tmp VALUES(?)", objFilmIds);
        String sqlQueryT007 = "SELECT * FROM t007_links_t001_t005 t007" +
                " JOIN t001_films t001 on t001.t001_id = t007.t001_id" +
                " WHERE t001.t001_id IN (SELECT id FROM films_tmp)";
        List<GenreToFilm> resultList = jdbcTemplate.query(sqlQueryT007, (rs, rowNum) -> mapRecordToGenreToFilm(rs));
        jdbcTemplate.update("DELETE FROM films_tmp");
        return resultList;
    }

    @Override
    public Map<Integer, List<Genre>> getMapOfGenresToFilms() {
        List<Genre> genres = getAll();
        List<GenreToFilm> genresToFilm = getAllGenresToFilm();
        Map<Integer, List<Genre>> resultMap = new HashMap<>();
        for (GenreToFilm link : genresToFilm) {
            if (resultMap.containsKey(link.getFilmId()))
                resultMap.get(link.getFilmId()).add(
                        genres.stream().filter(
                                x -> x.getId().equals(link.getGenreId())).findFirst().orElseThrow(() -> new NotFoundException(String.format("Жанр %d не найден!", link.getGenreId()))));
            else {
                List<Genre> newList = new ArrayList<>();
                newList.add(genres.stream().filter(
                        x -> x.getId().equals(link.getGenreId())).findFirst().orElseThrow(() -> new NotFoundException(String.format("Жанр %d не найден!", link.getGenreId()))));
                resultMap.put(link.getFilmId(), newList);
            }
        }
        return resultMap;
    }

    @Override
    public Map<Integer, List<Genre>> getMapOfGenresToFilmsByDirId(Integer dirId) {
        List<Genre> genres = getAll();
        List<GenreToFilm> genresToFilm = getAllGenresToFilmByDirId(dirId);
        Map<Integer, List<Genre>> resultMap = new HashMap<>();
        for (GenreToFilm link : genresToFilm) {
            if (resultMap.containsKey(link.getFilmId()))
                resultMap.get(link.getFilmId()).add(
                        genres.stream().filter(
                                x -> x.getId().equals(link.getGenreId())).findFirst().orElseThrow(() -> new NotFoundException(String.format("Жанр %d не найден!", link.getGenreId()))));
            else {
                List<Genre> newList = new ArrayList<>();
                newList.add(genres.stream().filter(
                        x -> x.getId().equals(link.getGenreId())).findFirst().orElseThrow(() -> new NotFoundException(String.format("Жанр %d не найден!", link.getGenreId()))));
                resultMap.put(link.getFilmId(), newList);
            }
        }
        return resultMap;
    }

    @Override
    public Map<Integer, List<Genre>> getMapOfGenresToFilmsByFilmIds(Collection<Integer> filmIds) {
        List<Genre> genres = getAll();
        List<GenreToFilm> genresToFilm = getAllGenresToFilmByFilmIds(filmIds);
        Map<Integer, List<Genre>> resultMap = new HashMap<>();
        for (GenreToFilm link : genresToFilm) {
            if (resultMap.containsKey(link.getFilmId()))
                resultMap.get(link.getFilmId()).add(
                        genres.stream().filter(
                                x -> x.getId().equals(link.getGenreId())).findFirst().orElseThrow(() -> new NotFoundException(String.format("Жанр %d не найден!", link.getGenreId()))));
            else {
                List<Genre> newList = new ArrayList<>();
                newList.add(genres.stream().filter(
                        x -> x.getId().equals(link.getGenreId())).findFirst().orElseThrow(() -> new NotFoundException(String.format("Жанр %d не найден!", link.getGenreId()))));
                resultMap.put(link.getFilmId(), newList);
            }
        }
        return resultMap;
    }

    @Override
    public List<Genre> getAllByFilmId(Integer filmId) {
        String query = "SELECT t005.* FROM t005_genres t005 JOIN t007_links_t001_t005 t007 on t007.t005_id = t005.t005_id WHERE t007.t001_id = ?";
        return jdbcTemplate.query(query, (rs, rowNum) -> mapRecordToGenre(rs), filmId);
    }

    private Genre mapRecordToGenre(ResultSet rs) {
        try {
            Integer id = rs.getInt("t005_id");
            String name = rs.getString("t005_name");
            return new Genre(id, name);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о жанре! Сообщение: %s", e.getMessage()));
        }
    }

    private GenreToFilm mapRecordToGenreToFilm(ResultSet rs) {
        try {
            Integer id = rs.getInt("t007_id");
            Integer genreId = rs.getInt("t005_id");
            Integer filmId = rs.getInt("t001_id");
            return new GenreToFilm(id, genreId, filmId);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о связи жанра и фильма! Сообщение: %s", e.getMessage()));
        }
    }
}
