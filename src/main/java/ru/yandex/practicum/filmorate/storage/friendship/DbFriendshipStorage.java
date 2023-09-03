package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("DbFriendshipStorage")
@Slf4j
@RequiredArgsConstructor
public class DbFriendshipStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Friendship getFriendship(Integer targetId, Integer friendId) {
        String sqlQueryT004 = "SELECT * FROM t004_friends t004 WHERE t002_target_id = ? AND t002_friend_id = ?";
        List<Friendship> resultList = jdbcTemplate.query(sqlQueryT004, (rs, rowNum) -> mapRecordToFriendship(rs), targetId, friendId);
        return resultList.stream().findFirst().orElse(null);
    }

    @Override
    public List<Integer> getFriendIdsByUserId(Integer targetId) {
        String sqlQueryT004 = "SELECT * FROM t004_friends t004 WHERE t002_target_id = ? OR (t002_friend_id = ? AND t004_confirmed = true)";
        List<Friendship> resultFriends = jdbcTemplate.query(sqlQueryT004, (rs, rowNum) -> mapRecordToFriendship(rs), targetId, targetId);
        List<Integer> resultList = new ArrayList<>();
        for (Friendship t004 : resultFriends) {
            if (targetId.equals(t004.getTargetId()))
                resultList.add(t004.getFriendId());
            else
                resultList.add(t004.getTargetId());
        }
        return resultList;
    }

    @Override
    public List<Integer> getCommonFriendIds(Integer targetId, Integer otherId) {
        List<Integer> targetList = getFriendIdsByUserId(targetId);
        List<Integer> otherListIds = getFriendIdsByUserId(otherId);
        return targetList.stream().filter(otherListIds::contains).collect(Collectors.toList());
    }

    @Override
    public Friendship addFriendship(Friendship friendship) {
        String sqlQueryT004 = "INSERT INTO t004_friends (t002_target_id, t002_friend_id, t004_confirmed) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQueryT004, new String[]{"t004_id"});
            statement.setInt(1, friendship.getTargetId());
            statement.setInt(2, friendship.getFriendId());
            statement.setBoolean(3, friendship.getConfirmed());
            return statement;
        }, keyHolder);
        friendship.setId(keyHolder.getKey().intValue());
        return friendship;
    }

    @Override
    public Friendship updateFriendship(Friendship friendship) {
        String sqlQueryT004 = "UPDATE t004_friends SET t002_target_id = ?, t002_friend_id = ?, t004_confirmed = ? WHERE t004_id = ?";
        jdbcTemplate.update(sqlQueryT004, friendship.getTargetId(), friendship.getFriendId(), friendship.getConfirmed(), friendship.getId());
        return friendship;
    }

    @Override
    public void deleteFriendship(Integer id) {
        String sqlQueryT004 = "DELETE FROM t004_friends WHERE t004_id = ?";
        jdbcTemplate.update(sqlQueryT004, id);
    }

    private Friendship mapRecordToFriendship(ResultSet rs) {
        try {
            Integer id = rs.getInt("t004_id");
            Integer targetId = rs.getInt("t002_target_id");
            Integer friendId = rs.getInt("t002_friend_id");
            Boolean confirmed = rs.getBoolean("t004_confirmed");
            return new Friendship(id, targetId, friendId, confirmed);
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о дружбе! Сообщение: %s", e.getMessage()));
        }
    }
}
