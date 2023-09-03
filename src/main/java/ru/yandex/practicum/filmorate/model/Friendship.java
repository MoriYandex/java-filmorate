package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Friendship {
    private Integer id;
    private Integer targetId;
    private Integer friendId;
    private Boolean confirmed;
}
