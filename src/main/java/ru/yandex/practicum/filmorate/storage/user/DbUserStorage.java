package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
@Slf4j
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final FriendshipStorage friendshipStorage;

    public DbUserStorage(JdbcTemplate jdbcTemplate, @Qualifier("DbFriendshipStorage") FriendshipStorage friendshipStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.friendshipStorage = friendshipStorage;
    }

    @Override
    public User getUser(Integer id) {
        String sqlQueryT002 = "SELECT * FROM t002_users WHERE t002_id = ?";
        List<User> resultList = jdbcTemplate.query(sqlQueryT002, (rs, rowNum) -> mapRecordToUser(rs), id);
        User user = resultList.stream().findFirst().orElse(null);
        if (user == null)
            return null;
        user.setFriends(new HashSet<>(friendshipStorage.getFriendIdsByUserId(id)));
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        String sqlQueryT002 = "SELECT * FROM t002_users";
        List<User> resultList = jdbcTemplate.query(sqlQueryT002, (rs, rowNum) -> mapRecordToUser(rs));
        for (User user : resultList)
            user.setFriends(new HashSet<>(friendshipStorage.getFriendIdsByUserId(user.getId())));
        return resultList;
    }

    @Override
    public User addUser(User user) {
        String sqlQueryT002 = "INSERT INTO t002_users (t002_email, t002_login, t002_name, t002_birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQueryT002, new String[]{"t002_id"});
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(4, Date.valueOf(user.getBirthday()));
            return statement;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sqlQueryT002 = "UPDATE t002_users SET t002_email = ?, t002_login = ?, t002_name = ?, t002_birthday = ? WHERE t002_id = ?";
        if (getUser(user.getId()) == null) {
            throw new NotFoundException(String.format("Пользователь %d не найден!", user.getId()));
        }
        jdbcTemplate.update(sqlQueryT002,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info(String.format("Пользователь %d успешно изменён.", user.getId()));
        return user;
    }

    @Override
    public User addFriend(Integer id, Integer friendId) {
        User targetUser = getUser(id);
        User friendUser = getUser(friendId);
        if (targetUser == null)
            throw new NotFoundException(String.format("Пользователь %d (исходный) не найден!", id));
        if (friendUser == null)
            throw new NotFoundException(String.format("Пользователь %d (друг) не найден!", friendId));
        if (targetUser.getFriends().contains(friendId))
            return targetUser;
        Friendship counterFriendship = friendshipStorage.getFriendship(friendId, id);
        if (counterFriendship == null)
            friendshipStorage.addFriendship(new Friendship(0, id, friendId, false));
        else if (!counterFriendship.getConfirmed()) {
            counterFriendship.setConfirmed(true);
            friendshipStorage.updateFriendship(counterFriendship);
        }
        targetUser.getFriends().add(friendId);
        addToFeedAddFriend(id, friendId);
        return targetUser;
    }

    @Override
    public User deleteFriend(Integer id, Integer friendId) {
        User targetUser = getUser(id);
        User friendUser = getUser(friendId);
        if (targetUser == null)
            throw new NotFoundException(String.format("Пользователь %d (исходный) не найден!", id));
        if (friendUser == null)
            throw new NotFoundException(String.format("Пользователь %d (друг) не найден!", friendId));
        if (!targetUser.getFriends().contains(friendId))
            return targetUser;
        Friendship directFriendship = friendshipStorage.getFriendship(id, friendId);
        if (directFriendship != null) {
            friendshipStorage.deleteFriendship(directFriendship.getId());
            if (directFriendship.getConfirmed())
                friendshipStorage.addFriendship(new Friendship(0, friendId, id, false));
        } else {
            Friendship counterFriendship = friendshipStorage.getFriendship(friendId, id);
            if (counterFriendship != null) {
                counterFriendship.setConfirmed(false);
                friendshipStorage.updateFriendship(counterFriendship);
            }
        }
        targetUser.getFriends().remove(friendId);
        addToFeedDeleteFriend(id, friendId);
        return targetUser;
    }

    @Override
    public List<User> getAllFriends(Integer id) {
        List<Integer> friendIds = friendshipStorage.getFriendIdsByUserId(id);
        return getUsersByIds(friendIds);
    }

    @Override
    public List<User> getCommonFriends(Integer id, Integer otherId) {
        List<Integer> friendIds = friendshipStorage.getCommonFriendIds(id, otherId);
        return getUsersByIds(friendIds);
    }

    @Override
    public User delete(Integer id) {
        User user = getUser(id);
        if (user == null) {
            throw new NotFoundException(String.format("Пользователя с id %d не существует.", id));
        }
        String query = "DELETE FROM t002_users WHERE t002_id = ?";
        jdbcTemplate.update(query, id);
        return user;
    }

    @Override
    public List<Feed> getUserFeed(Integer id) {
        String sqlQuery = "SELECT * FROM t011_feeds WHERE t011_user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::makeFeed, id);
    }

    private User mapRecordToUser(ResultSet rs) {
        try {
            Integer id = rs.getInt("t002_id");
            String email = rs.getString("t002_email");
            String login = rs.getString("t002_login");
            String name = rs.getString("t002_name");
            LocalDate birthday = rs.getDate("t002_birthday").toLocalDate();
            return new User(id, email, login, name, birthday, new HashSet<>());
        } catch (SQLException e) {
            throw new ValidationException(String.format("Неверная строка записи о пользователе! Сообщение: %s", e.getMessage()));
        }
    }

    private Feed makeFeed(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .userId(rs.getInt("t011_user_id"))
                .eventType(rs.getString("t011_event_type"))
                .operation(rs.getString("t011_operation"))
                .eventId(rs.getInt("t011_event_id"))
                .entityId(rs.getInt("t011_entity_id"))
                .timestamp(rs.getTimestamp("t011_timestamp"))
                .build();
    }

    private void addToFeedAddFriend(Integer userId, Integer friendId) {
        String sql = "INSERT INTO t011_feeds (t011_user_id, t011_event_type, t011_operation, t011_entity_id, t011_timestamp)" +
                " VALUES (?, 'FRIEND', 'ADD', ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, Date.from(Instant.now()));
    }

    private void addToFeedDeleteFriend(Integer userId, Integer friendId) {
        String sql = "INSERT INTO t011_feeds (t011_user_id, t011_event_type, t011_operation, t011_entity_id, t011_timestamp) " +
                "VALUES (?, 'FRIEND', 'REMOVE', ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, Date.from(Instant.now()));
    }

    private List<User> getUsersByIds(List<Integer> ids) {
        return getAllUsers().stream().filter(x -> ids.contains(x.getId())).collect(Collectors.toList());
    }
}
