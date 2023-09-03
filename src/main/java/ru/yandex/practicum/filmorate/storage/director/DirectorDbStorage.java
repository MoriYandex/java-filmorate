package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director createDirector(Director director) {
        String sqlQuery = "INSERT INTO t008_directors (t008_name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"t008_id"});
            statement.setString(1, director.getName());
            return statement;
        }, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        log.info(String.format("Добавлен режиссёр %s.", director));
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        if (isDirectorExists(director.getId())) {
            String updateDirector = "UPDATE t008_directors SET t008_name = ? WHERE t008_id = ?";
            jdbcTemplate.update(updateDirector, director.getName(), director.getId());
            log.info(String.format("Режиссёр %s обновлён.", director));
            return director;
        } else {
            log.warn(String.format("Режиссёр с id %d не найден.", director.getId()));
            throw new NotFoundException(String.format("Режиссёр с id %d не найден", director.getId()));
        }
    }

    @Override
    public void deleteDirector(int id) {
        if (isDirectorExists(id)) {
            String deleteDirector = "DELETE FROM t008_directors WHERE t008_id = ?";
            jdbcTemplate.update(deleteDirector, id);
            log.info(String.format("Режиссёр с id %d удалён.", id));
        } else {
            log.warn(String.format("Режиссёр с id %d не найден.", id));
            throw new NotFoundException(String.format("Режиссёр с id %d не найден", id));
        }
    }

    @Override
    public void deleteDirectorByFilm(int id) {
        if (isDirectorExists(id)) {
            String deleteDirector = "DELETE FROM t007_t001_t008 WHERE t001_id = ?";
            jdbcTemplate.update(deleteDirector, id);
            log.info(String.format("Режиссёр с id %d удалён.", id));
        } else {
            log.warn(String.format("Режиссёр с id %d не найден.", id));
        }
    }

    @Override
    public Director getDirectorById(int id) {
        String sqlQuery = "SELECT t008_id, t008_name FROM t008_directors WHERE t008_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, (rs, rowNum) -> makeDirector(rs), id);
        } catch (NotFoundException e) {
            log.error(String.format("Режиссёр с id %d не найден.", id));
            throw new NotFoundException(String.format("Режиссёр с id %d не найден", id));
        }
    }

    @Override
    public List<Director> getListAllDirectors() {
        String sqlQuery = "SELECT t008_id, t008_name FROM t008_directors";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeDirector(rs));
    }

    @Override
    public void load(List<Film> films) {
        final Map<Integer, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, f -> f));
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        final String sqlQuery = "SELECT t007_t001_t008.t001_id, t007_t001_t008.t008_id, t008_directors.t008_name " +
                "FROM t007_t001_t008 " +
                "JOIN t008_directors ON t007_t001_t008.t008_id = t008_directors.t008_id " +
                "WHERE t007_t001_t008.t001_id IN (" + inSql + ")";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            Film film = filmById.get(rs.getInt("t001_id"));
            film.addDirector(makeDirector(rs));
        }, films.stream().map(Film::getId).toArray());
    }

    @Override
    public boolean isDirectorExists(int id) {
        String sqlQuery = "SELECT t008_name FROM t008_directors WHERE t008_id = ?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        return directorRows.next();
    }

    @Override
    public void addDirector(Film film) {
        String addDirector = "INSERT INTO t007_t001_t008 (t001_id, t008_id) VALUES (?,?)";
        Set<Director> directors = film.getDirectors();
        for (Director director : directors) {
            if (!isDirectorExists(director.getId())) {
                String insertDirector = "INSERT INTO t008_directors (t008_name) VALUES (?)";
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement(insertDirector, new String[]{"t008_id"});
                    statement.setString(1, director.getName());
                    return statement;
                }, keyHolder);
                director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
                log.info(String.format("Добавлен режиссёр %s.", director));
            }
            jdbcTemplate.update(addDirector, film.getId(), director.getId());
        }
    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        int id = rs.getInt("t008_id");
        String name = rs.getString("t008_name");
        return Director.builder()
                .id(id)
                .name(name)
                .build();
    }

}
