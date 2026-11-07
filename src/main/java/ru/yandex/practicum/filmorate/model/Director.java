package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation_groups.CommonChecks;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    private Long id;

    @NotBlank(groups = CommonChecks.class, message = "Имя режиссера не может быть пустым")
    private String name;
}
