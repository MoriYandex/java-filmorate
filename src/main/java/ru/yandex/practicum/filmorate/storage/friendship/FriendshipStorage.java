package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;

public interface FriendshipStorage {
    Friendship getFriendship(Integer targetId, Integer friendId);

    List<Integer> getFriendIdsByUserId(Integer targetId);

    List<Integer> getCommonFriendIds(Integer targetId, Integer otherId);

    Friendship addFriendship(Friendship friendship);

    Friendship updateFriendship(Friendship friendship);

    void deleteFriendship(Integer id);
}
