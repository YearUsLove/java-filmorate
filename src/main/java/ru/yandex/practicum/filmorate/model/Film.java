package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.jackson.deserializers.DurationDeserializer;
import ru.yandex.practicum.filmorate.validation_groups.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NotEmptyObject(groups = NotEmptyBody.class)
public class Film {
    @Null(groups = OnCreate.class, message = "ID не должен быть указан")
    @NotNull(groups = OnUpdate.class, message = "ID должен быть указан")
    private Long id;

    @NotBlank(groups = CommonChecks.class, message = "Название фильма не должно быть пустым")
    private String name;

    @Size(groups = CommonChecks.class, min = 1, max = 200, message = "Описание не может содержать больше 200 символов")
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration duration;

    private Set<Genre> genres = new HashSet<>();

    @NotNull(groups = CommonChecks.class, message = "Фильм должен иметь рейтинг MPAA")
    private Rating mpa;

    @EqualsAndHashCode.Exclude
    private Set<Long> likes = new HashSet<>();

    @Positive(groups = CommonChecks.class, message = "Продолжительность фильма не может быть отрицательным числом")
    @JsonGetter("duration")
    public Long getDurationInMinutes() {
        if (duration == null) {
            return null;
        }
        return duration.toMinutes();
    }

    @JsonIgnore
    @AssertTrue(groups = CommonChecks.class, message = "Дата выпуска фильма не должна быть раньше 28.12.1895")
    public boolean isReleaseDateValid() {
        final LocalDate firstFilmRelease = LocalDate.of(1895, 12, 28);
        if (getReleaseDate() == null) {
            return true;
        }
        return !getReleaseDate().isBefore(firstFilmRelease);
    }
}
