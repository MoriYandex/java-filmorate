package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.feed.EventTypeEnum;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.OperationEnum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Feed> getAll() {
        String query = "SELECT * FROM t011_feeds";
        List<Feed> result = jdbcTemplate.query(query, (rs, rowNum) -> mapRowToFeed(rs));
        return result;
    }

    @Override
    public void createFeed(Feed feed) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("t011_feeds")
                .usingGeneratedKeyColumns("t011_event_id");
        feed.setEventId(simpleJdbcInsert.executeAndReturnKey(toMap(feed)).intValue());
    }

    private Map<String, Object> toMap(Feed feed) {
        Map<String, Object> values = new HashMap<>();
        values.put("t011_timestamp", feed.getTimestamp()); // #
        values.put("t011_user_id", feed.getUserId());
        values.put("t011_event_type", feed.getEventType());
        values.put("t011_operation", feed.getOperation());
        values.put("t011_entity_id", feed.getEntityId());
        return values;
    }

    private Feed mapRowToFeed(ResultSet resultSet) throws SQLException {
        Feed feed = Feed.builder()
//                .timestamp(resultSet.getTimestamp("t011_timestamp"))
                .timestamp(resultSet.getInt("t011_timestamp"))
                .userId(resultSet.getInt("t011_user_id"))
                .eventType(EventTypeEnum.valueOf(resultSet.getString("t011_event_type")))
                .operation(OperationEnum.valueOf(resultSet.getString("t011_operation")))
                .eventId(resultSet.getInt("t011_event_id"))
                .entityId(resultSet.getInt("t011_entity_id"))
                .build();
//
        return feed;
    }
}
