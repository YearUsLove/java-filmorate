package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long userId;
    private long otherUserId;
    private long filmId;

    @BeforeEach
    void setUp() {
        userId = createUser("author");
        otherUserId = createUser("reader");
        filmId = createFilm();
    }

    @Test
    void createAndGetReviewWithZeroRating() throws Exception {
        Map<String, Object> body = reviewBody(filmId);
        body.put("useful", 100);
        body.put("reviewId", 12345);
        JsonNode review = createReview(body);

        assertThat(review.get("reviewId").asLong()).isPositive().isNotEqualTo(12345);
        assertThat(review.get("useful").asInt()).isZero();
        assertThat(review.get("isPositive").asBoolean()).isFalse();
        mvc.perform(get("/reviews/{id}", review.get("reviewId").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Отзыв о фильме"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.filmId").value(filmId))
                .andExpect(jsonPath("$.isPositive").value(false))
                .andExpect(jsonPath("$.useful").value(0));
    }

    @Test
    void rejectMissingAndInvalidFields() throws Exception {
        for (String field : new String[]{"content", "isPositive", "userId", "filmId"}) {
            Map<String, Object> body = reviewBody(filmId);
            body.remove(field);
            mvc.perform(post("/reviews").contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
        Map<String, Object> body = reviewBody(filmId);
        body.put("content", "   ");
        mvc.perform(post("/reviews").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectUnknownAuthorAndFilm() throws Exception {
        for (String field : new String[]{"userId", "filmId"}) {
            Map<String, Object> body = reviewBody(filmId);
            body.put(field, Long.MAX_VALUE);
            mvc.perform(post("/reviews").contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isNotFound());
        }
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews", Integer.class)).isZero();
    }

    @Test
    void updatePreservesAuthorFilmAndRating() throws Exception {
        long id = createReview(reviewBody(filmId)).get("reviewId").asLong();
        mvc.perform(put("/reviews/{id}/like/{userId}", id, otherUserId)).andExpect(status().isOk());
        Map<String, Object> body = reviewBody(createFilm());
        body.put("reviewId", id);
        body.put("content", "Теперь понравился");
        body.put("isPositive", true);
        body.put("userId", otherUserId);
        body.put("useful", -100);

        mvc.perform(put("/reviews").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Теперь понравился"))
                .andExpect(jsonPath("$.isPositive").value(true))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.filmId").value(filmId))
                .andExpect(jsonPath("$.useful").value(1));
        mvc.perform(get("/reviews/{id}", id))
                .andExpect(jsonPath("$.content").value("Теперь понравился"))
                .andExpect(jsonPath("$.useful").value(1));
    }

    @Test
    void rejectUpdateWithoutIdAndUnknownReview() throws Exception {
        Map<String, Object> body = reviewBody(filmId);
        mvc.perform(put("/reviews").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
        body.put("reviewId", Long.MAX_VALUE);
        mvc.perform(put("/reviews").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void votesAreIdempotentAndCanChangeSign() throws Exception {
        long id = createReview(reviewBody(filmId)).get("reviewId").asLong();
        for (int i = 0; i < 2; i++) {
            mvc.perform(put("/reviews/{id}/like/{userId}", id, otherUserId)).andExpect(status().isOk());
        }
        assertUseful(id, 1);
        for (int i = 0; i < 2; i++) {
            mvc.perform(put("/reviews/{id}/dislike/{userId}", id, otherUserId)).andExpect(status().isOk());
        }
        assertUseful(id, -1);
        mvc.perform(put("/reviews/{id}/like/{userId}", id, userId)).andExpect(status().isOk());
        assertUseful(id, 0);
        mvc.perform(put("/reviews/{id}/like/{userId}", id, otherUserId)).andExpect(status().isOk());
        assertUseful(id, 2);
    }

    @Test
    void removeVotesThroughBothEndpoints() throws Exception {
        long id = createReview(reviewBody(filmId)).get("reviewId").asLong();
        for (String kind : new String[]{"like", "dislike"}) {
            mvc.perform(put("/reviews/{id}/{kind}/{userId}", id, kind, otherUserId))
                    .andExpect(status().isOk());
            for (int i = 0; i < 2; i++) {
                mvc.perform(delete("/reviews/{id}/{kind}/{userId}", id, kind, otherUserId))
                        .andExpect(status().isOk());
                assertUseful(id, 0);
            }
        }
        mvc.perform(put("/reviews/{id}/dislike/{userId}", id, otherUserId)).andExpect(status().isOk());
        mvc.perform(delete("/reviews/{id}/like/{userId}", id, otherUserId)).andExpect(status().isOk());
        assertUseful(id, 0);
    }

    @Test
    void deleteReviewAlsoDeletesVotes() throws Exception {
        long id = createReview(reviewBody(filmId)).get("reviewId").asLong();
        mvc.perform(put("/reviews/{id}/like/{userId}", id, otherUserId)).andExpect(status().isOk());
        mvc.perform(delete("/reviews/{id}", id)).andExpect(status().isOk());
        mvc.perform(get("/reviews/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(delete("/reviews/{id}", id)).andExpect(status().isNotFound());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_votes WHERE review_id = ?", Integer.class, id)).isZero();
    }

    @Test
    void listFiltersLimitsAndSortsByUsefulDescending() throws Exception {
        long negative = createReview(reviewBody(filmId)).get("reviewId").asLong();
        long neutral = createReview(reviewBody(filmId)).get("reviewId").asLong();
        long positive = createReview(reviewBody(filmId)).get("reviewId").asLong();
        createReview(reviewBody(createFilm()));
        mvc.perform(put("/reviews/{id}/dislike/{userId}", negative, otherUserId)).andExpect(status().isOk());
        mvc.perform(put("/reviews/{id}/like/{userId}", positive, otherUserId)).andExpect(status().isOk());

        mvc.perform(get("/reviews").param("filmId", Long.toString(filmId)).param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].reviewId").value(positive))
                .andExpect(jsonPath("$[0].useful").value(1))
                .andExpect(jsonPath("$[1].reviewId").value(neutral));
        mvc.perform(get("/reviews").param("filmId", Long.toString(filmId)))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[2].reviewId").value(negative))
                .andExpect(jsonPath("$[2].useful").value(-1));
        mvc.perform(get("/reviews")).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/reviews").param("filmId", Long.toString(Long.MAX_VALUE)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listDefaultsToTenAndUsesIdForEqualRatings() throws Exception {
        long firstId = createReview(reviewBody(filmId)).get("reviewId").asLong();
        for (int i = 0; i < 10; i++) {
            createReview(reviewBody(filmId));
        }
        mvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].reviewId").value(firstId));
    }

    @Test
    void rejectVotesForUnknownUserOrReview() throws Exception {
        long id = createReview(reviewBody(filmId)).get("reviewId").asLong();
        for (String kind : new String[]{"like", "dislike"}) {
            mvc.perform(put("/reviews/{id}/{kind}/{userId}", id, kind, Long.MAX_VALUE))
                    .andExpect(status().isNotFound());
            mvc.perform(put("/reviews/{id}/{kind}/{userId}", Long.MAX_VALUE, kind, userId))
                    .andExpect(status().isNotFound());
            mvc.perform(delete("/reviews/{id}/{kind}/{userId}", id, kind, Long.MAX_VALUE))
                    .andExpect(status().isNotFound());
            mvc.perform(delete("/reviews/{id}/{kind}/{userId}", Long.MAX_VALUE, kind, userId))
                    .andExpect(status().isNotFound());
        }
        assertUseful(id, 0);
    }

    @Test
    void rejectNonPositivePathAndQueryParameters() throws Exception {
        mvc.perform(get("/reviews/0")).andExpect(status().isBadRequest());
        mvc.perform(delete("/reviews/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/reviews").param("count", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/reviews").param("filmId", "-1")).andExpect(status().isBadRequest());
        mvc.perform(put("/reviews/1/like/0")).andExpect(status().isBadRequest());
    }

    private Map<String, Object> reviewBody(long reviewFilmId) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", "Отзыв о фильме");
        body.put("isPositive", false);
        body.put("userId", userId);
        body.put("filmId", reviewFilmId);
        return body;
    }

    private JsonNode createReview(Map<String, Object> body) throws Exception {
        String response = mvc.perform(post("/reviews").contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private void assertUseful(long reviewId, int useful) throws Exception {
        mvc.perform(get("/reviews/{id}", reviewId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.useful").value(useful));
    }

    private long createUser(String login) {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("USERS").usingGeneratedKeyColumns("ID")
                .executeAndReturnKey(Map.of("EMAIL", login + "@example.com", "LOGIN", login)).longValue();
    }

    private long createFilm() {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("FILMS").usingGeneratedKeyColumns("ID")
                .executeAndReturnKey(Map.of("TITLE", "Фильм", "RELEASE_DATE", LocalDate.of(2020, 1, 1),
                        "DURATION", 100, "RATING_ID", 1L)).longValue();
    }
}
