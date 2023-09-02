package ru.yandex.practicum.filmorate.model.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
public class Feed {
    private Integer eventId;
    private final Integer timestamp;
    private final Integer userId;
    private final EventTypeEnum eventType;
    private final OperationEnum operation;
    private final Integer entityId;
}
