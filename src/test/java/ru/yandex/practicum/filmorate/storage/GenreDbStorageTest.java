package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(GenreDbStorage.class)
class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAll();
        assertThat(genres).hasSize(6);
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreStorage.getById(1L).orElseThrow();
        assertThat(genre.getName()).isEqualTo("Комедия");
    }
}
