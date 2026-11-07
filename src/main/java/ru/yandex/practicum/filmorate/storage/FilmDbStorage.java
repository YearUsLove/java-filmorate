package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.extractors.FilmResultSetExtractor;

import javax.sql.DataSource;
import java.util.*;

import static org.springframework.util.ObjectUtils.isEmpty;

@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL_SQL = """
            SELECT f.*,
                   g.genres         AS genres,
                   d.directors      AS directors,
                   l.liked_user_ids AS liked_user_ids,
                   r.id             AS rating_id,
                   r.name           AS rating_name
            FROM films f
            JOIN ratings r ON r.id = f.rating_id
            LEFT JOIN (
                SELECT fg.film_id,
                       array_agg(DISTINCT gen.id || ':' || COALESCE(gen.name, '')) AS genres
                FROM film_genre fg
                JOIN genres gen ON gen.id = fg.genre_id
                GROUP BY fg.film_id
            ) g ON g.film_id = f.id
            LEFT JOIN (
                SELECT fd.film_id,
                       array_agg(DISTINCT d.id || ':' || COALESCE(d.name, '')) AS directors
                FROM film_director fd
                JOIN directors d ON d.id = fd.director_id
                GROUP BY fd.film_id
            ) d ON d.film_id = f.id
            LEFT JOIN (
                SELECT ls.film_id,
                       array_agg(DISTINCT ls.user_id) AS liked_user_ids
                FROM likes ls
                GROUP BY ls.film_id
            ) l ON l.film_id = f.id
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT f.*,
                   g.genres         AS genres,
                   d.directors      AS directors,
                   l.liked_user_ids AS liked_user_ids,
                   r.id             AS rating_id,
                   r.name           AS rating_name
            FROM films f
            JOIN ratings r ON r.id = f.rating_id
            LEFT JOIN (
                SELECT fg.film_id,
                       array_agg(DISTINCT gen.id || ':' || COALESCE(gen.name, '')) AS genres
                FROM film_genre fg
                JOIN genres gen ON gen.id = fg.genre_id
                WHERE fg.film_id = :id
                GROUP BY fg.film_id
            ) g ON g.film_id = f.id
            LEFT JOIN (
                SELECT fd.film_id,
                       array_agg(DISTINCT d.id || ':' || COALESCE(d.name, '')) AS directors
                FROM film_director fd
                JOIN directors d ON d.id = fd.director_id
                WHERE fd.film_id = :id
                GROUP BY fd.film_id
            ) d ON d.film_id = f.id
            LEFT JOIN (
                SELECT ls.film_id,
                       array_agg(DISTINCT ls.user_id) AS liked_user_ids
                FROM likes ls
                WHERE ls.film_id = :id
                GROUP BY ls.film_id
            ) l ON l.film_id = f.id
            WHERE f.id = :id
            """;

    private static final String FIND_BY_DIRECTOR_SQL = """
            SELECT f.*,
                   g.genres         AS genres,
                   d.directors      AS directors,
                   l.liked_user_ids AS liked_user_ids,
                   l.likes_count    AS likes_count,
                   r.id             AS rating_id,
                   r.name           AS rating_name
            FROM films f
            JOIN ratings r ON r.id = f.rating_id
            JOIN film_director fd ON fd.film_id = f.id
            LEFT JOIN (
                SELECT fg.film_id,
                       array_agg(DISTINCT gen.id || ':' || COALESCE(gen.name, '')) AS genres
                FROM film_genre fg
                JOIN genres gen ON gen.id = fg.genre_id
                GROUP BY fg.film_id
            ) g ON g.film_id = f.id
            LEFT JOIN (
                SELECT fd2.film_id,
                       array_agg(DISTINCT d.id || ':' || COALESCE(d.name, '')) AS directors
                FROM film_director fd2
                JOIN directors d ON d.id = fd2.director_id
                GROUP BY fd2.film_id
            ) d ON d.film_id = f.id
            LEFT JOIN (
                SELECT ls.film_id,
                       array_agg(DISTINCT ls.user_id) AS liked_user_ids,
                       count(DISTINCT ls.user_id)     AS likes_count
                FROM likes ls
                GROUP BY ls.film_id
            ) l ON l.film_id = f.id
            WHERE fd.director_id = :directorId
            """;

    private static final String UPDATE_FILM_SQL = """
            UPDATE films
            SET title        = :name,
                description  = :description,
                release_date = :releaseDate,
                duration     = :duration,
                rating_id    = :mpa.id
            WHERE id = :id
            """;

    private static final String DELETE_FILM_SQL = """
            DELETE FROM films WHERE id = :id
            """;

    private static final String INSERT_LIKE_SQL = """
            INSERT INTO likes (film_id, user_id)
            VALUES (:filmId, :userId)
            """;

    private static final String DELETE_LIKE_SQL = """
            DELETE FROM likes
            WHERE film_id = :filmId AND user_id = :userId
            """;

    private static final String MERGE_FILM_GENRE_SQL = """
            MERGE INTO film_genre (film_id, genre_id) KEY(film_id, genre_id)
            VALUES (:filmId, :genreId)
            """;

    private static final String MERGE_FILM_DIRECTOR_SQL = """
            MERGE INTO film_director (film_id, director_id) KEY(film_id, director_id)
            VALUES (:filmId, :directorId)
            """;

    private static final String MOST_POPULAR_FILMS_SQL = """
            SELECT f.*,
                   g.genres          AS genres,
                   d.directors       AS directors,
                   l.liked_user_ids  AS liked_user_ids,
                   l.likes_count     AS likes_count,
                   r.id              AS rating_id,
                   r.name            AS rating_name
            FROM films f
            JOIN ratings r ON r.id = f.rating_id
            LEFT JOIN (
                SELECT fg.film_id,
                       array_agg(DISTINCT gen.id || ':' || COALESCE(gen.name, '')) AS genres
                FROM film_genre fg
                JOIN genres gen ON gen.id = fg.genre_id
                GROUP BY fg.film_id
            ) g ON g.film_id = f.id
            LEFT JOIN (
                SELECT fd.film_id,
                       array_agg(DISTINCT d.id || ':' || COALESCE(d.name, '')) AS directors
                FROM film_director fd
                JOIN directors d ON d.id = fd.director_id
                GROUP BY fd.film_id
            ) d ON d.film_id = f.id
            LEFT JOIN (
                SELECT ls.film_id,
                       array_agg(DISTINCT ls.user_id) AS liked_user_ids,
                       count(DISTINCT ls.user_id)     AS likes_count
                FROM likes ls
                GROUP BY ls.film_id
            ) l ON l.film_id = f.id
            ORDER BY COALESCE(l.likes_count, 0) DESC, f.id
            LIMIT :count
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmResultSetExtractor extractor;
    private final DataSource dataSource;

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("ID");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("TITLE", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDurationInMinutes());
        parameters.put("RATING_ID", film.getMpa().getId());

        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        if (id == 0) {
            throw new RuntimeException("Фильм не был создан по неизвестной причине");
        }

        addFilmGenres(id, film.getGenres());
        addFilmDirectors(id, film.getDirectors());
        film.setId(id);

        return film;
    }

    @Override
    public void update(Film filmUpdate) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(filmUpdate);
        int updated = namedParameterJdbcTemplate.update(UPDATE_FILM_SQL, params);

        if (updated == 0) {
            throw new RuntimeException("Не удалось обновить фильм с id=" + filmUpdate.getId());
        }

        updateGenres(filmUpdate);
        updateDirectors(filmUpdate);
    }

    @Override
    public boolean delete(Long id) {
        int deleted = namedParameterJdbcTemplate.update(
                DELETE_FILM_SQL, new MapSqlParameterSource("id", id));
        return deleted > 0;
    }

    @Override
    public Collection<Film> findAll() {
        return namedParameterJdbcTemplate.query(FIND_ALL_SQL, extractor);
    }

    @Override
    public Optional<Film> findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Film> films = namedParameterJdbcTemplate.query(FIND_BY_ID_SQL, params, extractor);
        return films.stream().findFirst().map(this::normalize);
    }

    @Override
    public Collection<Film> findByDirectorId(Long directorId, String sortBy) {
        String orderBy = "year".equalsIgnoreCase(sortBy) ? "f.release_date" : "likes_count DESC, f.id";
        String sql = FIND_BY_DIRECTOR_SQL + " ORDER BY " + orderBy;
        return namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("directorId", directorId),
                extractor
        ).stream().map(this::normalize).toList();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(INSERT_LIKE_SQL, params);
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        int deleted = namedParameterJdbcTemplate.update(DELETE_LIKE_SQL, params);
        return deleted > 0;
    }

    @Override
    public Collection<Film> mostPopular(int count) {
        return namedParameterJdbcTemplate.query(
                MOST_POPULAR_FILMS_SQL,
                new MapSqlParameterSource("count", count),
                extractor
        ).stream().map(this::normalize).toList();
    }

    @Override
    public void clear() {
    }

    private Film normalize(Film film) {
        if (film.getLikes() == null) film.setLikes(new HashSet<>());
        if (film.getDirectors() == null) film.setDirectors(new HashSet<>());
        if (film.getGenres() == null) film.setGenres(new HashSet<>());
        return film;
    }

    private void updateGenres(Film film) {
        Long filmId = film.getId();
        namedParameterJdbcTemplate.update(
                "DELETE FROM film_genre WHERE film_id = :filmId",
                new MapSqlParameterSource("filmId", filmId));
        if (!isEmpty(film.getGenres())) {
            addFilmGenres(filmId, film.getGenres());
        }
    }

    private void updateDirectors(Film film) {
        Long filmId = film.getId();
        namedParameterJdbcTemplate.update(
                "DELETE FROM film_director WHERE film_id = :filmId",
                new MapSqlParameterSource("filmId", filmId));
        if (!isEmpty(film.getDirectors())) {
            addFilmDirectors(filmId, film.getDirectors());
        }
    }

    private void addFilmGenres(Long filmId, Set<Genre> genres) {
        if (isEmpty(genres)) return;
        SqlParameterSource[] mappingParams = genres.stream()
                .map(g -> new MapSqlParameterSource()
                        .addValue("filmId", filmId)
                        .addValue("genreId", g.getId()))
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(MERGE_FILM_GENRE_SQL, mappingParams);
    }

    private void addFilmDirectors(Long filmId, Set<Director> directors) {
        if (isEmpty(directors)) return;
        SqlParameterSource[] mappingParams = directors.stream()
                .map(d -> new MapSqlParameterSource()
                        .addValue("filmId", filmId)
                        .addValue("directorId", d.getId()))
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(MERGE_FILM_DIRECTOR_SQL, mappingParams);
    }
}
