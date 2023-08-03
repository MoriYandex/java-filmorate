package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final TreeMap<Integer, User> allUsers = new TreeMap<>();
    private Integer userIdSequence = 0;

    @GetMapping()
    public List<User> getAllUsers() {
        String USERS_LIST_GOT_MESSAGE = "Получен список всех пользователей.";
        log.info(USERS_LIST_GOT_MESSAGE);
        return new ArrayList<>(allUsers.values());
    }

    @PostMapping()
    public User addUser(@RequestBody User user) {
        validateUser(user);
        user.setId(++userIdSequence);
        allUsers.put(user.getId(), user);
        String USER_ADDED_MESSAGE = "Пользователь %d успешно добавлен.";
        log.info(USER_ADDED_MESSAGE, user.getId());
        return user;
    }

    @PutMapping()
    public User updateUser(@RequestBody User user) {
        validateUser(user);
        if (!allUsers.containsKey(user.getId()))
            user.setId(++userIdSequence);
        allUsers.put(user.getId(), user);
        String USER_UPDATED_MESSAGE = "Пользователь %d успешно изменён.";
        log.info(USER_UPDATED_MESSAGE, user.getId());
        return user;
    }

    public void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            String EMPTY_EMAIL_MESSAGE = "Электронная почта не должна быть пустой!";
            log.error(EMPTY_EMAIL_MESSAGE);
            throw new ValidationException(EMPTY_EMAIL_MESSAGE);
        }
        if (!user.getEmail().contains("@")) {
            String MISSING_DOG_MESSAGE = "Электронная почта должна содержать символ '@'!";
            log.error(MISSING_DOG_MESSAGE);
            throw new ValidationException(MISSING_DOG_MESSAGE);
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            String EMPTY_LOGIN_MESSAGE = "Логин не должен быть пустым!";
            log.error(EMPTY_LOGIN_MESSAGE);
            throw new ValidationException(EMPTY_LOGIN_MESSAGE);
        }
        if (user.getLogin().contains(" ")) {
            String LOGIN_WITH_WHITESPACE_MESSAGE = "Логин не должен содержать пробелы!";
            log.error(LOGIN_WITH_WHITESPACE_MESSAGE);
            throw new ValidationException(LOGIN_WITH_WHITESPACE_MESSAGE);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            String FUTURE_BIRTHDATE_MESSAGE = "Дата рождения не может быть в будущем!";
            log.error(FUTURE_BIRTHDATE_MESSAGE);
            throw new ValidationException(FUTURE_BIRTHDATE_MESSAGE);
        }
        if (user.getName() == null || user.getName().isBlank())
            user.setName(user.getLogin());
    }
}
