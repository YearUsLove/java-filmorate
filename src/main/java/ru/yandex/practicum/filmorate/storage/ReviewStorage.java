package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    void update(Review review);

    boolean delete(Long id);

    Optional<Review> findById(Long id);

    Collection<Review> findAll(Long filmId, int count);

    void setVote(Long reviewId, Long userId, int vote);

    void removeVote(Long reviewId, Long userId);
}
