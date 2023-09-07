package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User getUser(Integer id);

    List<User> getAllUsers();

    User addUser(User user);

    User updateUser(User user);

    User addFriend(Integer id, Integer friendId);

    User deleteFriend(Integer id, Integer friendId);

    List<User> getAllFriends(Integer id);

    List<User> getCommonFriends(Integer id, Integer otherId);

    User delete(Integer id);
}
