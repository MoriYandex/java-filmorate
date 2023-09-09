package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;


@Data
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
public class Feed {
    @NotNull
    private Integer eventId;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private final Date timestamp;
    @NotNull
    private final Integer userId;
    private final String eventType;
    private final String operation;
    @NotNull
    private final Integer entityId;
}
