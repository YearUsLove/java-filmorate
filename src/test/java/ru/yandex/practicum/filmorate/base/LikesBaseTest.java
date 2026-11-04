package ru.yandex.practicum.filmorate.base;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class LikesBaseTest extends BaseTest {

    @BeforeAll
    protected void setUp() {
        createUsers();
        createFilms();
        addLikes();
    }

    private void addLikes() {
        filmStorage.findById(2L).ifPresent(f -> f.getLikes().add(1L));
        filmStorage.findById(3L).ifPresent(f -> f.getLikes().add(1L));
        filmStorage.findById(3L).ifPresent(f -> f.getLikes().add(2L));
        filmStorage.findById(3L).ifPresent(f -> f.getLikes().add(3L));
        filmStorage.findById(3L).ifPresent(f -> f.getLikes().add(4L));
        filmStorage.findById(3L).ifPresent(f -> f.getLikes().add(5L));
        filmStorage.findById(3L).ifPresent(f -> f.getLikes().add(6L));
        filmStorage.findById(4L).ifPresent(f -> f.getLikes().add(6L));
        filmStorage.findById(4L).ifPresent(f -> f.getLikes().add(5L));
        filmStorage.findById(4L).ifPresent(f -> f.getLikes().add(4L));
    }
}
