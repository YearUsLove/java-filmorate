package ru.yandex.practicum.filmorate.storage.extractors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.RatingMPA;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class FilmResultSetExtractor implements ResultSetExtractor<List<Film>> {
    private static final String SEPARATOR = ":";

    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException {
        List<Film> films = new ArrayList<>();

        while (rs.next()) {
            Set<Genre> genres = new HashSet<>();
            Object[] genreArray = getArrayFromColumn(rs, "genres");
            if (!isEmpty(genreArray)) {
                for (Object obj : genreArray) {
                    if (obj == null) continue;
                    Genre genre = parseGenre(obj.toString());
                    if (genre != null) {
                        genres.add(genre);
                    }
                }
            }

            Set<Director> directors = null;
            Object[] directorArray = getArrayFromColumn(rs, "directors");
            if (!isEmpty(directorArray)) {
                directors = new HashSet<>();
                for (Object obj : directorArray) {
                    if (obj == null) continue;
                    Director director = parseDirector(obj.toString());
                    if (director != null) {
                        directors.add(director);
                    }
                }
            }

            Set<Long> likedUserIds = null;
            Object[] likeArray = getArrayFromColumn(rs, "liked_user_ids");
            if (likeArray != null) {
                likedUserIds = new HashSet<>();
                for (Object obj : likeArray) {
                    if (obj != null) {
                        likedUserIds.add(((Number) obj).longValue());
                    }
                }
            }

            Film film = Film.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("title"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .duration(Duration.ofMinutes(rs.getLong("duration")))
                    .mpa(parseRating(rs))
                    .genres(genres)
                    .directors(directors)
                    .likes(likedUserIds)
                    .build();

            films.add(film);
        }

        return films;
    }

    private Genre parseGenre(String raw) {
        int sepIndex = raw.indexOf(SEPARATOR);
        if (sepIndex <= 0) {
            log.warn("Повреждённая запись жанра: '{}'", raw);
            return null;
        }

        String idPart = raw.substring(0, sepIndex);
        String namePart = raw.substring(sepIndex + SEPARATOR.length());

        long genreId;
        try {
            genreId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            log.warn("Не удалось разобрать id жанра из '{}'", raw);
            return null;
        }

        return Genre.builder()
                .id(genreId)
                .name(isEmpty(namePart) ? null : namePart)
                .build();
    }

    private Director parseDirector(String raw) {
        int sepIndex = raw.indexOf(SEPARATOR);
        if (sepIndex <= 0) {
            log.warn("Повреждённая запись режиссёра: '{}'", raw);
            return null;
        }

        String idPart = raw.substring(0, sepIndex);
        String namePart = raw.substring(sepIndex + SEPARATOR.length());

        long directorId;
        try {
            directorId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            log.warn("Не удалось разобрать id режиссёра из '{}'", raw);
            return null;
        }

        return Director.builder()
                .id(directorId)
                .name(isEmpty(namePart) ? null : namePart)
                .build();
    }

    private Rating parseRating(ResultSet rs) throws SQLException {
        Long ratingId = rs.getLong("rating_id");
        String ratingName = rs.getString("rating_name");

        if (isEmpty(ratingName)) {
            return Rating.builder()
                    .id(ratingId)
                    .name(null)
                    .build();
        }

        RatingMPA ratingEnum = RatingMPA.fromNormalName(ratingName);
        if (isEmpty(ratingEnum)) {
            return Rating.builder()
                    .id(ratingId)
                    .name(null)
                    .build();
        }

        return Rating.builder()
                .id(ratingId)
                .name(ratingEnum)
                .build();
    }

    private Object[] getArrayFromColumn(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return switch (value) {
            case null -> null;
            case java.sql.Array array -> (Object[]) array.getArray();
            case Object[] objects -> objects;
            default -> throw new SQLException("Неожиданный тип для колонки " + columnName + ": " + value.getClass());
        };
    }
}
