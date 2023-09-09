package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface UserStorage {
    Boolean exists(Integer id);

    User get(Integer id);

    List<User> getAll();

    User add(User user);

    User update(User user);

    User delete(Integer id);

    User addFriend(Integer id, Integer friendId);

    User deleteFriend(Integer id, Integer friendId);

    List<User> getAllFriends(Integer id);

    List<User> getCommonFriends(Integer id, Integer otherId);

    List<Feed> getFeedsByUserId(Integer id);

    List<User> getMaxIntersectionUsers(Integer id);
}
