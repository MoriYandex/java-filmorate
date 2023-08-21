package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;
import java.util.Set;

@Data
@AllArgsConstructor
public class User {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private Date birthday;
    private Set<Integer> friends;
}
