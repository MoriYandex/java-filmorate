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
        String usersListGotMessage = "Получен список всех пользователей.";
        log.info(usersListGotMessage);
        return new ArrayList<>(allUsers.values());
    }

    @PostMapping()
    public User addUser(@RequestBody User user) {
        validateUser(user);
        user.setId(++userIdSequence);
        allUsers.put(user.getId(), user);
        String userAddedMessage = "Пользователь %d успешно добавлен.";
        log.info(userAddedMessage, user.getId());
        return user;
    }

    @PutMapping()
    public User updateUser(@RequestBody User user) {
        validateUser(user);
        if (!allUsers.containsKey(user.getId()))
            user.setId(++userIdSequence);
        allUsers.put(user.getId(), user);
        String userUpdatedMessage = "Пользователь %d успешно изменён.";
        log.info(userUpdatedMessage, user.getId());
        return user;
    }

    public void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            String emptyEmailMessage = "Электронная почта не должна быть пустой!";
            log.error(emptyEmailMessage);
            throw new ValidationException(emptyEmailMessage);
        }
        if (!user.getEmail().contains("@")) {
            String missingDogMessage = "Электронная почта должна содержать символ '@'!";
            log.error(missingDogMessage);
            throw new ValidationException(missingDogMessage);
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            String emptyLoginMessage = "Логин не должен быть пустым!";
            log.error(emptyLoginMessage);
            throw new ValidationException(emptyLoginMessage);
        }
        if (user.getLogin().contains(" ")) {
            String loginWithWhitespaceMessage = "Логин не должен содержать пробелы!";
            log.error(loginWithWhitespaceMessage);
            throw new ValidationException(loginWithWhitespaceMessage);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            String futureBirthdateMessage = "Дата рождения не может быть в будущем!";
            log.error(futureBirthdateMessage);
            throw new ValidationException(futureBirthdateMessage);
        }
        if (user.getName() == null || user.getName().isBlank())
            user.setName(user.getLogin());
    }
}
