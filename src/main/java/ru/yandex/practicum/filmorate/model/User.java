package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.enums.FriendStatus;
import ru.yandex.practicum.filmorate.validation_groups.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NotEmptyObject(groups = NotEmptyBody.class)
public class User {
    @Null(groups = OnCreate.class, message = "ID не должен быть указан")
    @NotNull(groups = OnUpdate.class, message = "ID должен быть указан")
    private Long id;

    @NotBlank(groups = CommonChecks.class, message = "Электронная почта пользователя не должна быть пустой")
    @Email(groups = CommonChecks.class, message = "Почта имеет некорректный формат")
    private String email;

    @NotBlank(groups = CommonChecks.class, message = "Логин не может быть пустым")
    @Pattern(groups = CommonChecks.class, regexp = "[\\w]*",
            message = "Логин должен содержать только латинские буквы и цифры")
    private String login;

    @Pattern(groups = CommonChecks.class, regexp = "[\\p{L}\\s.']*",
            message = "Имя пользователя должно содержать буквы и пробелы")
    private String name;

    @Past(groups = CommonChecks.class, message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @EqualsAndHashCode.Exclude
    private Map<Long, FriendStatus> friends = new HashMap<>();
}
