package ru.yandex.practicum.filmorate.tests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
class UserControllerTests {
    private final UserService userService = new UserService(new InMemoryUserStorage());

    @Test
    void validateUser() {
        final User user1 = new User(0, "", "login", "name", LocalDate.of(1983, 7, 1), new HashSet<>());
        assertThrows(ValidationException.class, () -> userService.validateUser(user1));
        final User user2 = new User(0, "wrongemail", "login", "name", LocalDate.of(1983, 7, 1), new HashSet<>());
        assertThrows(ValidationException.class, () -> userService.validateUser(user2));
        final User user3 = new User(0, "wrong@email", "  ", "name", LocalDate.of(1983, 7, 1), new HashSet<>());
        assertThrows(ValidationException.class, () -> userService.validateUser(user3));
        final User user4 = new User(0, "wrong@email", "1 2", "name", LocalDate.of(1983, 7, 1), new HashSet<>());
        assertThrows(ValidationException.class, () -> userService.validateUser(user4));
        final User user5 = new User(0, "wrong@email", "12", "name", LocalDate.of(2083, 7, 1), new HashSet<>());
        assertThrows(ValidationException.class, () -> userService.validateUser(user5));
        user1.setEmail("@");
        userService.validateUser(user1);
        assertEquals(user1.getEmail(), "@");
        user2.setEmail("@");
        userService.validateUser(user2);
        assertEquals(user2.getEmail(), "@");
        user3.setLogin("1");
        userService.validateUser(user3);
        assertEquals(user3.getLogin(), "1");
        user4.setLogin("12");
        userService.validateUser(user4);
        assertEquals(user4.getLogin(), "12");
        user5.setBirthday(LocalDate.now());
        userService.validateUser(user5);
        assertEquals(user5.getBirthday(), LocalDate.now());
        user3.setName("");
        userService.validateUser(user3);
        assertEquals(user3.getName(), user3.getLogin());
    }
}