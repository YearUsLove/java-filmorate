package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        Film existing = films.get(film.getId());
        if (film.getLikes() == null) {
            film.setLikes(existing.getLikes());
        }
        films.put(film.getId(), film);
    }

    @Override
    public boolean delete(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        films.remove(id);
        return true;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null && film.getLikes() != null) {
            film.getLikes().add(userId);
        }
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        return film != null && film.getLikes() != null && film.getLikes().remove(userId);
    }

    @Override
    public Collection<Film> mostPopular(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public Collection<Film> findByDirectorId(Long directorId, String sortBy) {
        List<Film> result = new ArrayList<>();
        for (Film film : films.values()) {
            if (film.getDirectors() != null && film.getDirectors().stream().anyMatch(d -> d.getId().equals(directorId))) {
                result.add(film);
            }
        }
        if ("year".equalsIgnoreCase(sortBy)) {
            result.sort(Comparator.comparing(Film::getReleaseDate));
        } else {
            result.sort(Comparator.comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size()).reversed());
        }
        return result;
    }

    @Override
    public void clear() {
        films.clear();
    }
}
