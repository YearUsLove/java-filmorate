package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(MpaDbStorage.class)
class MpaDbStorageTest {
    private final MpaDbStorage mpaStorage;

    @Test
    void testGetAllMpa() {
        List<MpaRating> list = mpaStorage.getAll();
        assertThat(list).hasSize(5);
    }

    @Test
    void testGetMpaById() {
        MpaRating mpa = mpaStorage.getById(3L).orElseThrow();
        assertThat(mpa.getName()).isEqualTo("PG-13");
    }
}
