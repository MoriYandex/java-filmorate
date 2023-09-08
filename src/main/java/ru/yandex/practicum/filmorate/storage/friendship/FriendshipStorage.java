package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;

public interface FriendshipStorage {
    Friendship get(Integer targetId, Integer friendId);

    List<Integer> getFriendsIdsByUserId(Integer targetId);

    List<Integer> getCommonFriendsIds(Integer targetId, Integer otherId);

    Friendship add(Friendship friendship);

    Friendship update(Friendship friendship);

    void delete(Integer id);
}
