package ru.yandex.practicum.filmorate.tests;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {
    UserController userController = new UserController();

    @Test
    void validateUser() {
        final User user1 = new User(0, "", "login", "name", LocalDate.of(1983, 7, 1));
        assertThrows(ValidationException.class, () -> userController.validateUser(user1));
        final User user2 = new User(0, "wrongemail", "login", "name", LocalDate.of(1983, 7, 1));
        assertThrows(ValidationException.class, () -> userController.validateUser(user2));
        final User user3 = new User(0, "wrong@email", "  ", "name", LocalDate.of(1983, 7, 1));
        assertThrows(ValidationException.class, () -> userController.validateUser(user3));
        final User user4 = new User(0, "wrong@email", "1 2", "name", LocalDate.of(1983, 7, 1));
        assertThrows(ValidationException.class, () -> userController.validateUser(user4));
        final User user5 = new User(0, "wrong@email", "12", "name", LocalDate.of(2083, 7, 1));
        assertThrows(ValidationException.class, () -> userController.validateUser(user5));
        user1.setEmail("@");
        userController.validateUser(user1);
        assertEquals(user1.getEmail(), "@");
        user2.setEmail("@");
        userController.validateUser(user2);
        assertEquals(user2.getEmail(), "@");
        user3.setLogin("1");
        userController.validateUser(user3);
        assertEquals(user3.getLogin(), "1");
        user4.setLogin("12");
        userController.validateUser(user4);
        assertEquals(user4.getLogin(), "12");
        user5.setBirthday(LocalDate.now());
        userController.validateUser(user5);
        assertEquals(user5.getBirthday(), LocalDate.now());
        user3.setName("");
        userController.validateUser(user3);
        assertEquals(user3.getName(), user3.getLogin());
    }
}