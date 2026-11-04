package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Film create(Film film);

    void update(Film filmUpdate);

    boolean delete(Long id);

    Collection<Film> findAll();

    Optional<Film> findById(Long id);

    void addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);

    Collection<Film> mostPopular(int count);

    void clear();
}
