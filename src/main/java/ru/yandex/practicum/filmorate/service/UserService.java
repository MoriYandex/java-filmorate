package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public User getUser(Integer id) {
        User user = userStorage.getUser(id);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        return user;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        validateUser(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        validateUser(user);
        return userStorage.updateUser(user);
    }

    public User addFriend(Integer id, Integer friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public User deleteFriend(Integer id, Integer friendId) {
        return userStorage.deleteFriend(id, friendId);
    }

    public List<User> getAllFriends(Integer id) {
        return userStorage.getAllFriends(id);
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public void validateUser(User user) {
        if (!StringUtils.hasText(user.getEmail())) {
            String emptyEmailMessage = "Электронная почта не должна быть пустой!";
            log.error(emptyEmailMessage);
            throw new ValidationException(emptyEmailMessage);
        }
        if (!user.getEmail().contains("@")) {
            String missingDogMessage = "Электронная почта должна содержать символ '@'!";
            log.error(missingDogMessage);
            throw new ValidationException(missingDogMessage);
        }
        if (!StringUtils.hasText(user.getLogin())) {
            String emptyLoginMessage = "Логин не должен быть пустым!";
            log.error(emptyLoginMessage);
            throw new ValidationException(emptyLoginMessage);
        }
        if (user.getLogin().contains(" ")) {
            String loginWithWhitespaceMessage = "Логин не должен содержать пробелы!";
            log.error(loginWithWhitespaceMessage);
            throw new ValidationException(loginWithWhitespaceMessage);
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            String futureBirthdateMessage = "Дата рождения не может быть в будущем!";
            log.error(futureBirthdateMessage);
            throw new ValidationException(futureBirthdateMessage);
        }
        if (!StringUtils.hasText(user.getName()))
            user.setName(user.getLogin());
    }
}
