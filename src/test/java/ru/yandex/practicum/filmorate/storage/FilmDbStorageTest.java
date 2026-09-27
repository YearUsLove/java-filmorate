package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film makeFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new MpaRating(1L, null));
        film.setGenres(new HashSet<>());
        return film;
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    @Test
    void testCreateAndFindFilm() {
        Film created = filmStorage.create(makeFilm("Inception"));
        Film found = filmStorage.getById(created.getId());
        assertThat(found.getName()).isEqualTo("Inception");
    }

    @Test
    void testFilmWithGenresAndMpa() {
        Film film = makeFilm("Test");
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1L, null));
        genres.add(new Genre(2L, null));
        film.setGenres(genres);

        Film created = filmStorage.create(film);
        Film found = filmStorage.getById(created.getId());

        assertThat(found.getGenres()).hasSize(2);
        assertThat(found.getMpa().getName()).isEqualTo("G");
    }

    @Test
    void testUpdateFilm() {
        Film created = filmStorage.create(makeFilm("Old"));
        created.setName("New");
        filmStorage.update(created);
        assertThat(filmStorage.getById(created.getId()).getName()).isEqualTo("New");
    }

    @Test
    void testAddAndRemoveLike() {
        User u1 = makeUser("a@a.ru", "a");
        User u2 = makeUser("b@b.ru", "b");
        Film film = filmStorage.create(makeFilm("Liked"));

        filmStorage.addLike(film.getId(), u1.getId());
        filmStorage.addLike(film.getId(), u2.getId());
        assertThat(filmStorage.getById(film.getId()).getLikes()).hasSize(2);

        filmStorage.removeLike(film.getId(), u1.getId());
        assertThat(filmStorage.getById(film.getId()).getLikes()).hasSize(1);
    }

    @Test
    void testGetPopular() {
        User u1 = makeUser("a@a.ru", "a");
        User u2 = makeUser("b@b.ru", "b");

        Film f1 = filmStorage.create(makeFilm("Popular"));
        Film f2 = filmStorage.create(makeFilm("LessPopular"));
        filmStorage.addLike(f1.getId(), u1.getId());
        filmStorage.addLike(f1.getId(), u2.getId());
        filmStorage.addLike(f2.getId(), u1.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(f1.getId());
    }

    @Test
    void testGetAllFilms() {
        filmStorage.create(makeFilm("A"));
        filmStorage.create(makeFilm("B"));
        assertThat(filmStorage.getAll()).hasSize(2);
    }

    @Test
    void testDeleteFilm() {
        Film created = filmStorage.create(makeFilm("ToDelete"));
        filmStorage.delete(created.getId());
        assertThat(filmStorage.getAll()).isEmpty();
    }
}
