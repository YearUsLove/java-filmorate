package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.validation_groups.OnCreate;
import ru.yandex.practicum.filmorate.validation_groups.OnUpdate;

import java.util.Collection;

@RestController
@Validated
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review create(@Validated(OnCreate.class) @RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Validated(OnUpdate.class) @RequestBody Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive Long id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable @Positive Long id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public Collection<Review> findAll(@RequestParam(required = false) @Positive Long filmId,
                                      @RequestParam(defaultValue = "10") @Positive int count) {
        return reviewService.findAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        reviewService.setVote(id, userId, 1);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        reviewService.setVote(id, userId, -1);
    }

    @DeleteMapping({"/{id}/like/{userId}", "/{id}/dislike/{userId}"})
    public void removeVote(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        reviewService.removeVote(id, userId);
    }
}
