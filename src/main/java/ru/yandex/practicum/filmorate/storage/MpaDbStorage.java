package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Component
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<MpaRating> MPA_MAPPER = (rs, rowNum) ->
            new MpaRating(rs.getLong("id"), rs.getString("name"));

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MpaRating> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa_ratings ORDER BY id", MPA_MAPPER);
    }

    @Override
    public Optional<MpaRating> getById(Long id) {
        List<MpaRating> list = jdbcTemplate.query(
                "SELECT id, name FROM mpa_ratings WHERE id = ?", MPA_MAPPER, id);
        return list.stream().findFirst();
    }
}
