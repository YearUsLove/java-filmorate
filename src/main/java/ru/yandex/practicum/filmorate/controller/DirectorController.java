package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.validation_groups.CommonChecks;

import java.util.Collection;

@Slf4j
@RestController
@Validated
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getAll() {
        log.info("Получен запрос на список всех режиссёров");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable @Positive(message = "id должен быть положительным") Long id) {
        log.info("Получен запрос на режиссёра с id={}", id);
        return directorService.getById(id);
    }

    @PostMapping
    public Director create(@Validated(CommonChecks.class) @RequestBody Director director) {
        log.info("Получен запрос на создание режиссёра: {}", director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@Validated(CommonChecks.class) @RequestBody Director director) {
        log.info("Получен запрос на обновление режиссёра: {}", director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive(message = "id должен быть положительным") Long id) {
        log.info("Получен запрос на удаление режиссёра с id={}", id);
        directorService.delete(id);
    }
}
