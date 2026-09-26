package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Genre> GENRE_MAPPER = (rs, rowNum) ->
            new Genre(rs.getLong("id"), rs.getString("name"));

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", GENRE_MAPPER);
    }

    @Override
    public Optional<Genre> getById(Long id) {
        List<Genre> list = jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id = ?", GENRE_MAPPER, id);
        return list.stream().findFirst();
    }
}
