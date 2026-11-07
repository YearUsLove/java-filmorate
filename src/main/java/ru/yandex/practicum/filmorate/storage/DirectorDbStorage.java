package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;

    private static final String FIND_ALL_SQL = "SELECT * FROM directors";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM directors WHERE id = :id";
    private static final String UPDATE_SQL = "UPDATE directors SET name = :name WHERE id = :id";
    private static final String DELETE_SQL = "DELETE FROM directors WHERE id = :id";

    @Override
    public Collection<Director> findAll() {
        return namedParameterJdbcTemplate.query(FIND_ALL_SQL, new BeanPropertyRowMapper<>(Director.class));
    }

    @Override
    public Optional<Director> findById(Long id) {
        List<Director> directors = namedParameterJdbcTemplate.query(
                FIND_BY_ID_SQL,
                new MapSqlParameterSource("id", id),
                new BeanPropertyRowMapper<>(Director.class)
        );
        return directors.stream().findFirst();
    }

    @Override
    public Director create(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> params = new HashMap<>();
        params.put("name", director.getName());

        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        int updated = namedParameterJdbcTemplate.update(
                UPDATE_SQL,
                new MapSqlParameterSource()
                        .addValue("id", director.getId())
                        .addValue("name", director.getName())
        );
        if (updated == 0) {
            throw new RuntimeException("Не удалось обновить режиссёра с id=" + director.getId());
        }
        return director;
    }

    @Override
    public boolean delete(Long id) {
        int deleted = namedParameterJdbcTemplate.update(
                DELETE_SQL,
                new MapSqlParameterSource("id", id)
        );
        return deleted > 0;
    }
}
