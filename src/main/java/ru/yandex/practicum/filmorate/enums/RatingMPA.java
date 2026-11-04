package ru.yandex.practicum.filmorate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum RatingMPA {
    G("G", "у фильма нет возрастных ограничений"),
    PG("PG", "детям рекомендуется смотреть фильм с родителями"),
    PG_13("PG-13", "детям до 13 лет просмотр не желателен"),
    R("R", "лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17("NC-17", "лицам до 18 лет просмотр запрещён");

    private final String normalName;
    @Getter
    private final String description;

    @JsonValue
    public String getNormalName() {
        return normalName;
    }

    private static final Map<String, RatingMPA> BY_NORMAL_NAME =
            Arrays.stream(values())
                    .collect(Collectors.toMap(RatingMPA::getNormalName, Function.identity()));

    @JsonCreator
    public static RatingMPA fromNormalName(String normalName) {
        if (normalName == null) {
            return null;
        }
        RatingMPA rating = BY_NORMAL_NAME.get(normalName);
        if (rating == null) {
            throw new IllegalArgumentException("Неизвестный рейтинг: " + normalName);
        }
        return rating;
    }
}
