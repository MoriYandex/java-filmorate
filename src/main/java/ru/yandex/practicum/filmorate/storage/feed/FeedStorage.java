package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.feed.Feed;

import java.util.List;


public interface FeedStorage {
//    Feed get(Integer id);

    void createFeed(Feed feed);

    List<Feed> getAll();
}
