package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String FILM_SELECT =
            "SELECT f.*, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id ";

    private static final RowMapper<Film> FILM_MAPPER = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }
        film.setDuration(rs.getInt("duration"));
        long mpaId = rs.getLong("mpa_rating_id");
        if (!rs.wasNull()) {
            film.setMpa(new MpaRating(mpaId, rs.getString("mpa_name")));
        }
        film.setLikes(new LinkedHashSet<>());
        film.setGenres(new LinkedHashSet<>());
        return film;
    };

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbcTemplate.query(FILM_SELECT + "ORDER BY f.id", FILM_MAPPER);
        loadAllGenresAndLikes(films);
        return films;
    }

    @Override
    public Film getById(Long id) {
        List<Film> films = jdbcTemplate.query(FILM_SELECT + "WHERE f.id = ?", FILM_MAPPER, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        Film film = films.get(0);
        loadAllGenresAndLikes(List.of(film));
        return film;
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Film create(Film film) {
        validateMpaAndGenres(film);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration() != null ? film.getDuration() : 0);
            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        saveGenres(film);
        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        if (!existsById(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        validateMpaAndGenres(film);
        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                        "WHERE id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return getById(film.getId());
    }

    @Override
    public void delete(Long id) {
        int deleted = jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
        if (deleted == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Film> films = jdbcTemplate.query(
                FILM_SELECT +
                        "ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC, f.id ASC " +
                        "LIMIT ?",
                FILM_MAPPER, count);
        loadAllGenresAndLikes(films);
        return films;
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?",
                    Integer.class, film.getMpa().getId());
            if (count == null || count == 0) {
                throw new NotFoundException("Рейтинг с id=" + film.getMpa().getId() + " не найден");
            }
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    Integer count = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM genres WHERE id = ?",
                            Integer.class, genre.getId());
                    if (count == null || count == 0) {
                        throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                    }
                }
            }
        }
    }

    private void loadAllGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Film> byId = new HashMap<>();
        for (Film f : films) {
            byId.put(f.getId(), f);
        }

        jdbcTemplate.query(
                "SELECT fg.film_id, g.id, g.name " +
                        "FROM film_genres fg " +
                        "JOIN genres g ON fg.genre_id = g.id " +
                        "ORDER BY fg.film_id, g.id",
                rs -> {
                    Film film = byId.get(rs.getLong("film_id"));
                    if (film != null) {
                        film.getGenres().add(new Genre(rs.getLong("id"), rs.getString("name")));
                    }
                });

        jdbcTemplate.query(
                "SELECT film_id, user_id FROM film_likes ORDER BY film_id",
                rs -> {
                    Film film = byId.get(rs.getLong("film_id"));
                    if (film != null) {
                        film.getLikes().add(rs.getLong("user_id"));
                    }
                });
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            if (genre.getId() != null) {
                jdbcTemplate.update(
                        "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }
}
