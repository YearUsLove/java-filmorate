package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaStorage mpaStorage;

    public MpaController(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @GetMapping
    public List<MpaRating> getAll() {
        return mpaStorage.getAll();
    }

    @GetMapping("/{id}")
    public MpaRating getById(@PathVariable Long id) {
        return mpaStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + id + " не найден"));
    }
}
