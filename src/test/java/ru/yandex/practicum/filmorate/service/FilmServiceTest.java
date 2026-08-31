package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private Film film1;
    private Film film2;
    private User user;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);

        film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        filmStorage.create(film1);

        film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(120);
        filmStorage.create(film2);

        user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(user);
    }

    @Test
    void addLike() {
        filmService.addLike(film1.getId(), user.getId());

        assertTrue(filmStorage.getById(film1.getId()).getLikes().contains(user.getId()));
    }

    @Test
    void addLikeTwiceShouldBeIdempotent() {
        filmService.addLike(film1.getId(), user.getId());
        filmService.addLike(film1.getId(), user.getId());

        assertEquals(1, filmStorage.getById(film1.getId()).getLikes().size());
    }

    @Test
    void removeLike() {
        filmService.addLike(film1.getId(), user.getId());
        filmService.removeLike(film1.getId(), user.getId());

        assertFalse(filmStorage.getById(film1.getId()).getLikes().contains(user.getId()));
    }

    @Test
    void getPopular() {
        filmService.addLike(film1.getId(), user.getId());
        // film2 has no likes

        List<Film> popular = filmService.getPopular(10);

        assertEquals(film1.getId(), popular.get(0).getId());
        assertEquals(film2.getId(), popular.get(1).getId());
    }
}