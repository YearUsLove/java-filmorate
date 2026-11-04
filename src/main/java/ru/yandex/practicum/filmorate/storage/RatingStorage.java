package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

public interface RatingStorage {

    Collection<Rating> findAll();

    Rating findById(Long id);
}
