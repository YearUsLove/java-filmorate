package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.enums.RatingMPA;
import ru.yandex.practicum.filmorate.validation_groups.NotEmptyBody;
import ru.yandex.practicum.filmorate.validation_groups.NotEmptyObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NotEmptyObject(groups = NotEmptyBody.class)
public class Rating {
    private Long id;
    private RatingMPA name;
}
