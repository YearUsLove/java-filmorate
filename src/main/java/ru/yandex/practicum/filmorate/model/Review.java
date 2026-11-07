package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation_groups.CommonChecks;
import ru.yandex.practicum.filmorate.validation_groups.OnUpdate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @NotNull(groups = OnUpdate.class, message = "ID отзыва должен быть указан")
    @Positive(groups = CommonChecks.class, message = "ID отзыва должен быть положительным числом")
    private Long reviewId;

    @NotBlank(groups = CommonChecks.class, message = "Содержание отзыва не должно быть пустым")
    private String content;

    @NotNull(groups = CommonChecks.class, message = "Тип отзыва должен быть указан")
    @JsonProperty("isPositive")
    private Boolean isPositive;

    @NotNull(groups = CommonChecks.class, message = "ID пользователя должен быть указан")
    private Long userId;

    @NotNull(groups = CommonChecks.class, message = "ID фильма должен быть указан")
    private Long filmId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int useful;
}
