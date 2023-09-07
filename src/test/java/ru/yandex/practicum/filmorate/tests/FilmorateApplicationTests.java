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

import static org.junit.jupiter.api.Assertions.*;

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
        test4Likes();
    }

    public void test1Users() {
        User user1 = new User(0, "a@b.ru", "login", "name", LocalDate.of(1983, 7, 1), new HashSet<>());
        userStorage.addUser(user1);
        User user2 = userStorage.getUser(1);
        assertEquals(user1.getId(), 1);
        user2.setEmail("d@e.ru");
        userStorage.updateUser(user2);
        User user3 = userStorage.getUser(1);
        assertEquals(user3.getEmail(), "d@e.ru");
        userStorage.addUser(user3);
        List<User> users = userStorage.getAllUsers();
        assertEquals(users.size(), 2);
        assertNull(userStorage.getUser(3));
        user3.setLogin("login2");
        userStorage.addUser(user3);
        users = userStorage.getAllUsers();
        assertEquals(users.size(), 3);
    }

    public void test2Friends() {
        User user1 = userStorage.getUser(1);
        userStorage.addFriend(user1.getId(), 2);
        List<User> friends1 = userStorage.getAllFriends(1);
        assertEquals(friends1.size(), 1);
        List<User> friends2 = userStorage.getAllFriends(2);
        assertEquals(friends2.size(), 0);
        userStorage.addFriend(2, 1);
        Friendship friendship = friendshipStorage.getFriendship(1, 2);
        assertEquals(friendship.getConfirmed(), true);
        assertNull(friendshipStorage.getFriendship(2, 1));
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
        friendship = friendshipStorage.getFriendship(1, 3);
        assertEquals(friendship.getConfirmed(), false);
        assertNull(friendshipStorage.getFriendship(3, 1));
    }

    public void test3Films() {
        List<Genre> genres = new ArrayList<>();
        genres.add(genreStorage.getGenre(1));
        genres.add(genreStorage.getGenre(3));
        Film film1 = new Film(5,
                "Film1",
                "description1",
                LocalDate.now(),
                100,
                genres,
                ratingStorage.getRating(2),
                new HashSet<>(), new ArrayList<>());
        film1 = filmStorage.addFilm(film1);
        List<Film> films1 = filmStorage.getAllFilms();
        assertEquals(films1.size(), 1);
        assertEquals(films1.get(0).getGenres().size(), 2);
        assertEquals(films1.get(0).getId(), 1);
        assertEquals(film1.getId(), 1);
        genres = genreStorage.getAllGenres();
        film1.setGenres(genres);
        filmStorage.updateFilm(film1);
        film1 = filmStorage.getFilm(1);
        assertEquals(film1.getGenres().size(), 6);
        Rating rating = ratingStorage.getRating(3);
        film1.setMpa(rating);
        filmStorage.addFilm(film1);
        films1 = filmStorage.getAllFilms();
        assertEquals(films1.size(), 2);
        Film film2 = filmStorage.getFilm(2);
        assertEquals(film2.getMpa().getId(), 3);
        List<Rating> ratings = ratingStorage.getAllRatings();
        film2.setMpa(ratings.get(3));
        filmStorage.addFilm(film2);
        films1 = filmStorage.getAllFilms();
        assertEquals(films1.size(), 3);
        assertEquals(films1.get(2).getMpa().getId(), 4);
    }

    public void test4Likes() {
        filmStorage.addLike(1, 1);
        filmStorage.addLike(2, 1);
        filmStorage.addLike(3, 1);
        filmStorage.addLike(2, 2);
        filmStorage.addLike(2, 2);
        filmStorage.addLike(2, 2);
        filmStorage.addLike(3, 2);
        filmStorage.addLike(3, 3);
        List<Film> films = filmStorage.getMostPopular(3);
        assertEquals(films.get(0).getId(), 3);
        assertEquals(films.get(1).getId(), 2);
        assertEquals(films.get(2).getId(), 1);
        assertEquals(films.get(0).getLikesCount(), 3);
        assertEquals(films.get(1).getLikesCount(), 2);
        assertEquals(films.get(2).getLikesCount(), 1);
        filmStorage.deleteLike(2, 1);
        filmStorage.deleteLike(2, 2);
        filmStorage.deleteLike(2, 3);
        films = filmStorage.getMostPopular(10);
        assertEquals(films.get(0).getId(), 3);
        assertEquals(films.get(1).getId(), 1);
        assertEquals(films.get(2).getId(), 2);
        assertEquals(films.get(0).getLikesCount(), 3);
        assertEquals(films.get(1).getLikesCount(), 1);
        assertEquals(films.get(2).getLikesCount(), 0);
        assertEquals(films.size(), 3);

    }
}
