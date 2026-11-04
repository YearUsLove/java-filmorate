package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.base.FilmsBaseTest;
import ru.yandex.practicum.filmorate.controller.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilmGenresTests extends FilmsBaseTest {

    @Test
    void getAllGenresTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/genres"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /genres должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        Set<Genre> gens = objectMapper.readValue(body, new TypeReference<>() {
        });

        assertThat(gens).as("Полный список жанров")
                .withFailMessage("Полученный список из запроса должен совпадать с заданным")
                .containsExactlyInAnyOrderElementsOf(genresPreset);
    }

    @Test
    void getGenreByIdTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/genres/4"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /genres/4 должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        Genre gen = objectMapper.readValue(body, Genre.class);

        assertThat(gen).as("Жанр по id=4").isNotNull();
        assertThat(gen.getName()).as("Наименование жанра под id=4")
                .isNotEmpty()
                .isEqualTo("Триллер");
    }

    @Test
    void getGenreByUnknownIdTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/genres/10000"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "GET /genres/10000 должен вернуть 404");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        ErrorResponse err = objectMapper.readValue(body, ErrorResponse.class);

        assertThat(err).as("Сообщение об ошибке").isNotNull();
        assertThat(err.getError()).as("Сообщение о несуществующем id")
                .isNotEmpty()
                .isEqualTo("Жанр по id=10000 не найден");
    }

    @Test
    void getFilmWithGenreTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/2"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /films/2 должен вернуть 200");

        String body = resp.body().trim();
        Film film = objectMapper.readValue(body, Film.class);

        assertThat(film).as("Фильм по id=2").isNotNull();
        assertThat(film.getGenres()).as("Жанр фильма с id=2")
                .isNotEmpty()
                .containsExactly(Genre.builder().id(4L).name("Триллер").build());
    }

    @Test
    void getFilmWithMultipleGenresTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/3"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /films/3 должен вернуть 200");

        String body = resp.body().trim();
        Film film = objectMapper.readValue(body, Film.class);

        assertThat(film).as("Фильм по id=3").isNotNull();
        assertThat(film.getGenres()).as("Жанры фильма с id=3")
                .isNotEmpty()
                .containsExactlyInAnyOrder(Genre.builder().id(1L).name("Комедия").build(),
                        Genre.builder().id(4L).name("Триллер").build());
    }

    @Test
    void getFilmWithoutGenresTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/1"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /films/1 должен вернуть 200");

        String body = resp.body().trim();
        Film film = objectMapper.readValue(body, Film.class);

        assertThat(film).as("Фильм по id=1").isNotNull();
        assertThat(film.getGenres()).as("Жанры фильма с id=1")
                .isEmpty();
    }

    @Test
    void getFilmWithoutDuplicateGenresTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/4"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /films/4 должен вернуть 200");

        String body = resp.body().trim();
        Film film = objectMapper.readValue(body, Film.class);

        assertThat(film).as("Фильм по id=4").isNotNull();
        assertThat(film.getGenres()).as("Жанры фильма с id=4")
                .isNotEmpty()
                .containsExactlyInAnyOrder(Genre.builder().id(1L).name("Комедия").build(),
                        Genre.builder().id(4L).name("Триллер").build());
    }
}
