package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final TreeMap<Integer, User> allUsers = new TreeMap<>();
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
            throw new ValidationException("Пользователь не найден!");
        allUsers.put(user.getId(), user);
        log.info(String.format("Пользователь %d успешно изменён.", user.getId()));
        return user;
    }

    @Override
    public User addFriend(Integer id, Integer friendId) {
        User user1 = getUser(id);
        User user2 = getUser(friendId);
        if (user1 == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        if (user2 == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", friendId));
        if (!user1.getFriends().contains(user2.getId()) && !user2.getFriends().contains(user1.getId())) {
            user1.getFriends().add(user2.getId());
            user2.getFriends().add(user1.getId());
        }
        return user1;
    }

    @Override
    public User deleteFriend(Integer id, Integer friendId) {
        User user1 = getUser(id);
        User user2 = getUser(friendId);
        if (user1 == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        if (user2 == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", friendId));
        if (user1.getFriends().contains(user2.getId()) && user2.getFriends().contains(user1.getId())) {
            user1.getFriends().remove(user2.getId());
            user2.getFriends().remove(user1.getId());
        }
        return user1;
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
        User user1 = getUser(id);
        User user2 = getUser(otherId);
        if (user1 == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        if (user2 == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", otherId));
        return getAllUsers()
                .stream().filter(x -> user1.getFriends().contains(x.getId())
                        && user2.getFriends().contains(x.getId()))
                .collect(Collectors.toList());
    }
}
