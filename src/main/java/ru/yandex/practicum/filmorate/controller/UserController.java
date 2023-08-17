package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public User getUser(@PathVariable Integer id){
        log.info("UserController: поиск пользователя");
        return userService.getUser(id);
    }

    @GetMapping()
    public List<User> getAllUsers() {
        log.info("UserController: вызов списка всех пользователей");
        return userService.getAllUsers();
    }

    @PostMapping()
    public User addUser(@RequestBody User user) {
        log.info("UserController: добавление пользователя");
        return userService.addUser(user);
    }

    @PutMapping()
    public User updateUser(@RequestBody User user) {
        log.info("UserController: добавление пользователя");
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriends(@PathVariable Integer id, @PathVariable Integer friendId){
        log.info("UserController: добавление в друзья");
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriends(@PathVariable Integer id, @PathVariable Integer friendId){
        log.info("UserController: удаление из друзей");
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable Integer id){
        log.info("UserController: поиск друзей");
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId){
        log.info("UserController: поиск общих друзей");
        return userService.getCommonFriends(id, otherId);
    }
}
