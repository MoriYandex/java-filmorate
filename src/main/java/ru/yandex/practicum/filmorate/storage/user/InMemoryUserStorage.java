package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("InMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> allUsers = new HashMap<>();
    private Integer userIdSequence = 0;

    @Override
    public User getUser(Integer id) {
        return allUsers.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Получен список всех пользователей.");
        return new ArrayList<>(allUsers.values());
    }

    @Override
    public User addUser(User user) {
        user.setId(++userIdSequence);
        allUsers.put(user.getId(), user);
        log.info(String.format("Пользователь %d успешно добавлен.", user.getId()));
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!allUsers.containsKey(user.getId()))
            throw new NotFoundException(String.format("Пользователь %d не найден!", user.getId()));
        allUsers.put(user.getId(), user);
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
        if (!targetUser.getFriends().contains(friendUser.getId()) && !friendUser.getFriends().contains(targetUser.getId())) {
            targetUser.getFriends().add(friendUser.getId());
            friendUser.getFriends().add(targetUser.getId());
        }
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
        if (targetUser.getFriends().contains(friendUser.getId()) && friendUser.getFriends().contains(targetUser.getId())) {
            targetUser.getFriends().remove(friendUser.getId());
            friendUser.getFriends().remove(targetUser.getId());
        }
        return targetUser;
    }

    @Override
    public List<User> getAllFriends(Integer id) {
        User user = getUser(id);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        return getAllUsers()
                .stream().filter(x -> user.getFriends().contains(x.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Integer id, Integer otherId) {
        User targetUser = getUser(id);
        User otherUser = getUser(otherId);
        if (targetUser == null)
            throw new NotFoundException(String.format("Пользователь %d (первый) не найден!", id));
        if (otherUser == null)
            throw new NotFoundException(String.format("Пользователь %d (второй) не найден!", otherId));
        return getAllUsers()
                .stream().filter(x -> targetUser.getFriends().contains(x.getId())
                        && otherUser.getFriends().contains(x.getId()))
                .collect(Collectors.toList());
    }
}
