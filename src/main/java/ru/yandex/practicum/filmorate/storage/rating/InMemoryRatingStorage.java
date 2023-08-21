package ru.yandex.practicum.filmorate.storage.rating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Qualifier("InMemoryRatingStorage")
@Slf4j
public class InMemoryRatingStorage implements RatingStorage {
    private final HashMap<Integer, Rating> allRatings = new HashMap<>();

    public InMemoryRatingStorage() {
        allRatings.put(1, new Rating(1, "G", "у фильма нет возрастных ограничений"));
        allRatings.put(2, new Rating(2, "PG", "детям рекомендуется смотреть фильм с родителями"));
        allRatings.put(3, new Rating(3, "PG-13", "детям до 13 лет просмотр не желателен"));
        allRatings.put(4, new Rating(4, "R", "лицам до 17 лет просматривать фильм можно только в присутствии взрослого"));
        allRatings.put(5, new Rating(5, "NC-17", "лицам до 18 лет просмотр запрещён"));
    }

    @Override
    public Rating getRating(Integer id) {
        return allRatings.get(id);
    }

    @Override
    public List<Rating> getAllRatings() {
        return new ArrayList<>(allRatings.values());
    }
}
