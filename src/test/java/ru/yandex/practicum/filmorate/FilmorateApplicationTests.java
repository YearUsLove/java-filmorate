package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FilmorateApplicationTests {

    @Test
    void mainClassExists() {
        assertDoesNotThrow(() -> Class.forName("ru.yandex.practicum.filmorate.FilmorateApplication"));
    }
}
