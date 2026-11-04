package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("inMemoryStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        Long key = IdGenerator.getNextId(films.keySet());
        film.setId(key);
        return films.put(key, film);
    }

    @Override
    public void update(Film filmUpdate) {
        Film currentFilm = films.get(filmUpdate.getId());

        currentFilm.setDescription(filmUpdate.getDescription());
        currentFilm.setName(filmUpdate.getName());
        currentFilm.setReleaseDate(filmUpdate.getReleaseDate());
        currentFilm.setDuration(filmUpdate.getDuration());
        currentFilm.setGenres(filmUpdate.getGenres());
    }

    @Override
    public boolean delete(Long id) {
        return films.remove(id) != null;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return findAll().stream()
                .filter(f -> f.getId().equals(id))
                .findAny();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        findById(filmId).ifPresent(f -> f.getLikes().add(userId));
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        return findById(filmId).stream()
                .peek(f -> f.getLikes().remove(userId))
                .noneMatch(f -> f.getLikes().contains(userId));
    }

    @Override
    public Collection<Film> mostPopular(int count) {
        return films.values().stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        films.clear();
    }
}
