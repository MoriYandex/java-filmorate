package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.*;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private final FilmService filmService;

    @GetMapping("/{id}")
    public User get(@PathVariable Integer id) {
        return userService.getUser(id);
    }

    @GetMapping()
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @PostMapping()
    public User add(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping()
    public User update(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("/{userId}")
    public User delete(@PathVariable Integer userId) {
        return userService.deleteUser(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriends(@PathVariable Integer id, @PathVariable Integer friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriends(@PathVariable Integer id, @PathVariable Integer friendId) {
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable Integer id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{userId}/recommendations")
    public List<Film> getRecommendations(@PathVariable Integer userId) {
        return filmService.getRecommendFilms(userId);
    }

    @GetMapping("/{id}/feed")
    public List<Feed> getFeedsByUserId(@PathVariable("id") Integer id) {
        return userService.getFeedsByUserId(id);
    }
}
