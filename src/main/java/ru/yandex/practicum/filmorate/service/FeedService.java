package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.feed.EventTypeEnum;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.OperationEnum;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    @Autowired
    public FeedService(FeedStorage feedStorage, UserStorage userStorage) {
        this.feedStorage = feedStorage;
        this.userStorage = userStorage;
    }

    public List<Feed> getFeeds(Integer id) {
        if (isExist(id)) {
            log.info("Пользователь с id {} был успешно найден, подгружаю его события...", id);
            return feedStorage.getAll();
        } else {
            throw new NotFoundException("FeedService | Пользователя с таким id не существует.");
        }
    }

    public Optional<Feed> toFeed(Integer entityId, Integer userId,
                                     EventTypeEnum eventType, OperationEnum operation) {
        Feed feed = new Feed(
                new Timestamp(new Date().getTime()).getNanos(),
                userId,
                eventType,
                operation,
                entityId
        );
        feedStorage.createFeed(feed);
        log.info("Событие {} успешно создано.", feed);
        return Optional.of(feed);
    }

    private boolean isExist(int id) {
        for (User user : userStorage.getAllUsers()) {
            if (id == user.getId()) {
                return true;
            }
        }
        return false;
    }
}
