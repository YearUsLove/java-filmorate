package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;

@Primary
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private static final String FIND_ALL_GENRES_SQL = """
            SELECT * FROM genres
            """;

    private static final String FIND_BY_ID_GENRE_SQL = """
            SELECT * FROM genres
            WHERE id = :genreId
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Collection<Genre> findAll() {
        return namedParameterJdbcTemplate.query(FIND_ALL_GENRES_SQL, genreRowMapper);
    }

    @Override
    public Genre findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("genreId", id);
        List<Genre> genres = namedParameterJdbcTemplate.query(FIND_BY_ID_GENRE_SQL, params, genreRowMapper);

        return genres.stream().findAny().orElseThrow(() ->
                new NotFoundException("Жанр по id=" + id + " не найден"));
    }
}
