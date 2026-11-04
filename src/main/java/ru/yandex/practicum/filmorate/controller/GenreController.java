package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Slf4j
@RestController
@Validated
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreStorage genreStorage;

    @GetMapping()
    public Collection<Genre> genres() {
        log.info("Получен запрос на получение списка всех жанров.");

        Collection<Genre> all = genreStorage.findAll();
        log.debug("Всего жанров: {}. Список жанров: {}", all.size(), all);
        return all;
    }

    @GetMapping("/{id}")
    public Genre genre(@PathVariable
                       @Positive(message = "id жанра должен быть положительным числом") Long id) {
        log.debug("Поиск жанра по id={}", id);
        Genre genre = genreStorage.findById(id);
        log.debug("Жанр {} найден по id={}", genre, id);

        return genre;
    }
}
