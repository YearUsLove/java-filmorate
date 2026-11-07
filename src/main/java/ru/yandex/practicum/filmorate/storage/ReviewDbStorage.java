package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private static final String SELECT_REVIEWS_SQL = """
            SELECT r.*, COALESCE(v.useful, 0) AS useful
            FROM reviews r
            LEFT JOIN (
                SELECT review_id, SUM(vote) AS useful
                FROM review_votes
                GROUP BY review_id
            ) v ON v.review_id = r.id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final ReviewRowMapper rowMapper;

    @Override
    public Review create(Review review) {
        long id = new SimpleJdbcInsert(dataSource)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("ID")
                .executeAndReturnKey(Map.of(
                        "CONTENT", review.getContent(),
                        "IS_POSITIVE", review.getIsPositive(),
                        "USER_ID", review.getUserId(),
                        "FILM_ID", review.getFilmId()))
                .longValue();
        review.setReviewId(id);
        review.setUseful(0);
        return review;
    }

    @Override
    public void update(Review review) {
        jdbcTemplate.update("""
                UPDATE reviews SET content = :content, is_positive = :isPositive
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", review.getReviewId())
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive()));
    }

    @Override
    public boolean delete(Long id) {
        return jdbcTemplate.update("DELETE FROM reviews WHERE id = :id", Map.of("id", id)) > 0;
    }

    @Override
    public Optional<Review> findById(Long id) {
        return jdbcTemplate.query(SELECT_REVIEWS_SQL + " WHERE r.id = :id", Map.of("id", id), rowMapper)
                .stream().findFirst();
    }

    @Override
    public Collection<Review> findAll(Long filmId, int count) {
        String filter = filmId == null ? "" : " WHERE r.film_id = :filmId";
        return jdbcTemplate.query(SELECT_REVIEWS_SQL + filter + " ORDER BY useful DESC, r.id LIMIT :count",
                new MapSqlParameterSource("filmId", filmId).addValue("count", count), rowMapper);
    }

    @Override
    public void setVote(Long reviewId, Long userId, int vote) {
        jdbcTemplate.update("""
                MERGE INTO review_votes (review_id, user_id, vote) KEY(review_id, user_id)
                VALUES (:reviewId, :userId, :vote)
                """, new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userId", userId)
                .addValue("vote", vote));
    }

    @Override
    public void removeVote(Long reviewId, Long userId) {
        jdbcTemplate.update("DELETE FROM review_votes WHERE review_id = :reviewId AND user_id = :userId",
                Map.of("reviewId", reviewId, "userId", userId));
    }
}
