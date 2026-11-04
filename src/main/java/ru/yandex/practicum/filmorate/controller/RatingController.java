package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.Collection;

@Slf4j
@RestController
@Validated
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingController {
    private final RatingStorage ratingStorage;

    @GetMapping()
    public Collection<Rating> ratings() {
        log.info("Получен запрос на получение списка всех рейтингов.");

        Collection<Rating> all = ratingStorage.findAll();
        log.debug("Всего записей с рейтингами: {}. Список рейтингов: {}", all.size(), all);
        return all;
    }

    @GetMapping("/{id}")
    public Rating rating(@PathVariable
                         @Positive(message = "id рейтинга должен быть положительным числом") Long id) {
        log.debug("Поиск рейтинга по id={}", id);
        Rating rating = ratingStorage.findById(id);
        log.debug("Рейтинг {} найден по id={}", rating, id);

        return rating;
    }
}
