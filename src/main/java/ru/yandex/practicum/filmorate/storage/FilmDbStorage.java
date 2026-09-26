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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
            film.setMpaRating(new MpaRating(mpaId, null));
        }
        film.setLikes(new HashSet<>());
        film.setGenres(new HashSet<>());
        return film;
    };

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbcTemplate.query(
                "SELECT * FROM films ORDER BY id", FILM_MAPPER);
        for (Film film : films) {
            loadGenresAndLikes(film);
        }
        return films;
    }

    @Override
    public Film getById(Long id) {
        List<Film> films = jdbcTemplate.query(
                "SELECT * FROM films WHERE id = ?", FILM_MAPPER, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        Film film = films.get(0);
        loadGenresAndLikes(film);
        return film;
    }

    @Override
    public Film create(Film film) {
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
            if (film.getMpaRating() != null) {
                ps.setLong(5, film.getMpaRating().getId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        saveGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        int updated = jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                        "WHERE id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().getId() : null,
                film.getId());
        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
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
                "SELECT f.* FROM films f " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                        "GROUP BY f.id " +
                        "ORDER BY COUNT(fl.user_id) DESC " +
                        "LIMIT ?",
                FILM_MAPPER, count);
        for (Film film : films) {
            loadGenresAndLikes(film);
        }
        return films;
    }

    private void loadGenresAndLikes(Film film) {
        // жанры
        List<Genre> genres = jdbcTemplate.query(
                "SELECT g.id, g.name FROM genres g " +
                        "JOIN film_genres fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ? ORDER BY g.id",
                (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")),
                film.getId());
        film.setGenres(new HashSet<>(genres));

        // MPA (если только id был задан)
        if (film.getMpaRating() != null && film.getMpaRating().getName() == null) {
            jdbcTemplate.query(
                    "SELECT name FROM mpa_ratings WHERE id = ?",
                    rs -> {
                        if (rs.next()) {
                            film.getMpaRating().setName(rs.getString("name"));
                        }
                    },
                    film.getMpaRating().getId());
        }

        // лайки
        List<Long> likes = jdbcTemplate.query(
                "SELECT user_id FROM film_likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getLong("user_id"),
                film.getId());
        film.setLikes(new HashSet<>(likes));
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
