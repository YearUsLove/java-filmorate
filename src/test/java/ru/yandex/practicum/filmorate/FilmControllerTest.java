package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Интерстеллар");
        validFilm.setDescription("Космический фильм");
        validFilm.setReleaseDate(LocalDate.of(2014, 11, 7));
        validFilm.setDuration(169);
    }

    @Test
    void createValidFilm() {
        Film createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm.getId());
        assertEquals(validFilm.getName(), createdFilm.getName());
        assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    void createFilmWithEmptyName() {
        validFilm.setName("");
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void createFilmWithNullName() {
        validFilm.setName(null);
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void createFilmWithTooLongDescription() {
        validFilm.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void createFilmWithDescriptionExactly200Characters() {
        validFilm.setDescription("a".repeat(200));
        Film createdFilm = filmController.createFilm(validFilm);
        assertEquals(200, createdFilm.getDescription().length());
    }

    @Test
    void createFilmWithEarlyReleaseDate() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void createFilmWithReleaseDateExactly28December1895() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        Film createdFilm = filmController.createFilm(validFilm);
        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate());
    }

    @Test
    void createFilmWithZeroDuration() {
        validFilm.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void createFilmWithNegativeDuration() {
        validFilm.setDuration(-100);
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void updateNonExistentFilm() {
        validFilm.setId(999L);
        assertThrows(ValidationException.class, () -> filmController.updateFilm(validFilm));
    }

    @Test
    void updateExistingFilm() {
        Film createdFilm = filmController.createFilm(validFilm);
        createdFilm.setName("Обновленное название");
        Film updatedFilm = filmController.updateFilm(createdFilm);
        assertEquals("Обновленное название", updatedFilm.getName());
    }
}