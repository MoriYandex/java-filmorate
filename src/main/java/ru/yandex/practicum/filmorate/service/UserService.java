package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User getUser(Integer id) {
        log.info("Поиск пользователя по идентификатору {}", id);
        User user = userStorage.get(id);
        if (user == null)
            throw new NotFoundException(String.format("Пользователь %d не найден!", id));
        return user;
    }

    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.getAll();
    }

    public User addUser(User user) {
        log.info("Добавление пользователя");
        validateUser(user);
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        log.info("Изменение данных пользователя по идентификатору {}", user.getId());
        validateUser(user);
        return userStorage.update(user);
    }

    public User deleteUser(Integer id) {
        log.info("Удаление пользователя");
        return userStorage.delete(id);
    }

    public User addFriend(Integer id, Integer friendId) {
        log.info("Добавление пользователем {} друга {}", id, friendId);
        if (id < 0 || friendId < 0) {
            throw new NotFoundException("Идентификаторы не могут быть отрицательными.");
        }
        return userStorage.addFriend(id, friendId);
    }

    public User deleteFriend(Integer id, Integer friendId) {
        log.info("Удаление пользователем {} друга {}", id, friendId);
        if (id < 0 || friendId < 0) {
            throw new ValidationException("Идентификаторы не могут быть отрицательными.");
        }
        return userStorage.deleteFriend(id, friendId);
    }

    public List<User> getAllFriends(Integer id) {
        if (exists(id)) {
            log.info("Получение списка друзей пользователя {}", id);
            return userStorage.getAllFriends(id);
        } else {
            throw new NotFoundException(String.format("Пользователя с id %d не существует.", id));
        }
    }

    public List<User> getCommonFriends(Integer id, Integer otherId) {
        if (id < 0 || otherId < 0) {
            throw new ValidationException("Идентификаторы не могут быть отрицательными.");
        }
        log.info("Получение списка друзей пользователя {}, общих с пользователем {}", id, otherId);
        return userStorage.getCommonFriends(id, otherId);
    }

    public List<Feed> getFeedsByUserId(Integer userId) {
        if (exists(userId)) {
            List<Feed> userFeed = userStorage.getFeedsByUserId(userId);
            log.info("Новостная лента пользователя {}: {} {}", userId, userStorage.get(userId).getName(), userFeed);
            return userFeed;
        } else {
            throw new NotFoundException("Такого пользователя не существует.");
        }
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

    private Boolean exists(int id) {
        return userStorage.exists(id);
    }
}
