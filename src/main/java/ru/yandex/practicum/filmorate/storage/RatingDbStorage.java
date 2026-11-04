package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.Collection;
import java.util.List;

@Primary
@Repository
@RequiredArgsConstructor
public class RatingDbStorage implements RatingStorage {

    private static final String FIND_ALL_RATINGS_SQL = """
            SELECT * FROM ratings
            ORDER BY id
            LIMIT 5
            """;

    private static final String FIND_BY_ID_RATING_SQL = """
            SELECT * FROM ratings
            WHERE id = :ratingId
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RatingRowMapper ratingRowMapper;

    @Override
    public Collection<Rating> findAll() {
        return namedParameterJdbcTemplate.query(FIND_ALL_RATINGS_SQL, ratingRowMapper);
    }

    @Override
    public Rating findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("ratingId", id);
        List<Rating> ratings = namedParameterJdbcTemplate.query(FIND_BY_ID_RATING_SQL, params, ratingRowMapper);

        return ratings.stream().findAny().orElseThrow(() ->
                new NotFoundException("Рейтинг по id=" + id + " не найден"));
    }
}
