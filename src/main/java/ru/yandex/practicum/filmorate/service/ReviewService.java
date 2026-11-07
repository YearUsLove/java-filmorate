package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Transactional
    public Review create(Review review) {
        checkUser(review.getUserId());
        filmStorage.findById(review.getFilmId()).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + review.getFilmId() + " не найден"));
        return reviewStorage.create(review);
    }

    @Transactional
    public Review update(Review review) {
        getById(review.getReviewId());
        reviewStorage.update(review);
        return getById(review.getReviewId());
    }

    @Transactional
    public void delete(Long id) {
        if (!reviewStorage.delete(id)) {
            throw new NotFoundException("Отзыв с id = " + id + " не найден");
        }
    }

    public Review getById(Long id) {
        return reviewStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Отзыв с id = " + id + " не найден"));
    }

    public Collection<Review> findAll(Long filmId, int count) {
        return reviewStorage.findAll(filmId, count);
    }

    @Transactional
    public void setVote(Long id, Long userId, int vote) {
        getById(id);
        checkUser(userId);
        reviewStorage.setVote(id, userId, vote);
    }

    @Transactional
    public void removeVote(Long id, Long userId) {
        getById(id);
        checkUser(userId);
        reviewStorage.removeVote(id, userId);
    }

    private void checkUser(Long userId) {
        userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}
