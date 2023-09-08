package ru.yandex.practicum.filmorate.tests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.storage.friendship.DbFriendshipStorage;
import ru.yandex.practicum.filmorate.storage.genre.DbGenreStorage;
import ru.yandex.practicum.filmorate.storage.rating.DbRatingStorage;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final DbFilmStorage filmStorage;
    private final DbUserStorage userStorage;
    private final DbGenreStorage genreStorage;
    private final DbRatingStorage ratingStorage;
    private final DbFriendshipStorage friendshipStorage;

    @Test
    void contextLoads() {
    }

    @Test
    public void testAll() {
        test1Users();
        test2Friends();
        test3Films();
    }

    public void test1Users() {
        User user1 = new User(0, "a@b.ru", "login", "name", LocalDate.of(1983, 7, 1), new HashSet<>());
        userStorage.add(user1);
        User user2 = userStorage.get(1);
        assertEquals(user1.getId(), 1);
        user2.setEmail("d@e.ru");
        userStorage.update(user2);
        User user3 = userStorage.get(1);
        assertEquals(user3.getEmail(), "d@e.ru");
        userStorage.add(user3);
        List<User> users = userStorage.getAll();
        assertEquals(users.size(), 2);
        assertNull(userStorage.get(3));
        user3.setLogin("login2");
        userStorage.add(user3);
        users = userStorage.getAll();
        assertEquals(users.size(), 3);
    }

    public void test2Friends() {
        User user1 = userStorage.get(1);
        userStorage.addFriend(user1.getId(), 2);
        List<User> friends1 = userStorage.getAllFriends(1);
        assertEquals(friends1.size(), 1);
        List<User> friends2 = userStorage.getAllFriends(2);
        assertEquals(friends2.size(), 0);
        userStorage.addFriend(2, 1);
        Friendship friendship = friendshipStorage.get(1, 2);
        assertEquals(friendship.getConfirmed(), true);
        assertNull(friendshipStorage.get(2, 1));
        List<User> friends3 = userStorage.getCommonFriends(1, 2);
        assertEquals(friends3.size(), 0);
        userStorage.addFriend(1, 2);
        userStorage.addFriend(3, 1);
        List<User> friends4 = userStorage.getCommonFriends(1, 2);
        assertEquals(friends4.size(), 0);
        List<User> friends5 = userStorage.getCommonFriends(2, 3);
        assertEquals(friends5.size(), 1);
        assertEquals(friends5.get(0).getId(), 1);
        userStorage.addFriend(1, 3);
        userStorage.addFriend(2, 3);
        friends3 = userStorage.getCommonFriends(1, 2);
        assertEquals(friends3.size(), 1);
        userStorage.deleteFriend(3, 1);
        friendship = friendshipStorage.get(1, 3);
        assertEquals(friendship.getConfirmed(), false);
        assertNull(friendshipStorage.get(3, 1));
    }

    public void test3Films() {
        List<Genre> genres = new ArrayList<>();
        genres.add(genreStorage.get(1));
        genres.add(genreStorage.get(3));
        Film film1 = new Film(5,
                "Film1",
                "description1",
                LocalDate.now(),
                100,
                genres,
                ratingStorage.getRating(2),
                new HashSet<>(), new ArrayList<>());
        film1 = filmStorage.add(film1);
        List<Film> films1 = filmStorage.getAll();
        assertEquals(films1.size(), 1);
        assertEquals(films1.get(0).getGenres().size(), 2);
        assertEquals(films1.get(0).getId(), 1);
        assertEquals(film1.getId(), 1);
        genres = genreStorage.getAll();
        film1.setGenres(genres);
        filmStorage.update(film1);
        film1 = filmStorage.get(1);
        assertEquals(film1.getGenres().size(), 6);
        Rating rating = ratingStorage.getRating(3);
        film1.setMpa(rating);
        filmStorage.add(film1);
        films1 = filmStorage.getAll();
        assertEquals(films1.size(), 2);
        Film film2 = filmStorage.get(2);
        assertEquals(film2.getMpa().getId(), 3);
        List<Rating> ratings = ratingStorage.getAllRatings();
        film2.setMpa(ratings.get(3));
        filmStorage.add(film2);
        films1 = filmStorage.getAll();
        assertEquals(films1.size(), 3);
        assertEquals(films1.get(2).getMpa().getId(), 4);
    }
}
