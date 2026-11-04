package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.base.LikesBaseTest;
import ru.yandex.practicum.filmorate.controller.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FilmLikesTests extends LikesBaseTest {

    @Test
    public void addLikeTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/1/like/1"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "PUT /films/1/like/1 должен вернуть 200");

        req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));
        String body = resp.body().trim();
        List<Film> films = objectMapper.readValue(body, new TypeReference<>() {
        });
        Set<Long> likes = films.stream()
                .filter(f -> f.getId().equals(1L))
                .findFirst()
                .map(Film::getLikes)
                .orElse(new HashSet<>());

        assertFalse(likes.isEmpty(), "Список лайков фильма с id=1 не должен быть пустым");
        assertEquals(1, likes.size(), "Количество лайков фильма с id=1 должно быть равно 1");
        assertTrue(likes.contains(1L), "Список лайков фильма с id=1 должен содержать лайк пользователя с id=1");
    }

    @Test
    public void addLikeToUnknownFilmTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/999/like/1"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "PUT /films/999/like/1 должен вернуть 404");
    }

    @Test
    public void addLikeFromUnknownUserTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/1/like/999"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "PUT /films/1/like/999 должен вернуть 404");

        req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));
        String body = resp.body().trim();
        List<Film> films = objectMapper.readValue(body, new TypeReference<>() {
        });
        Set<Long> likes = films.stream()
                .filter(f -> f.getId().equals(1L))
                .findFirst()
                .map(Film::getLikes)
                .orElse(new HashSet<>());

        assertTrue(likes.isEmpty() || !likes.contains(999L),
                "Список лайков фильма с id=1 не должен содержать лайк от пользователя с id=999");
    }

    @Test
    public void removeLikeTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/2/like/1"))
                .DELETE()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "DELETE /films/2/like/1 должен вернуть 200");

        req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));
        String body = resp.body().trim();
        List<Film> films = objectMapper.readValue(body, new TypeReference<>() {
        });
        Set<Long> likes = films.stream()
                .filter(f -> f.getId().equals(1L))
                .findFirst()
                .map(Film::getLikes)
                .orElse(new HashSet<>());

        assertTrue(likes.isEmpty() || !likes.contains(1L),
                "Список лайков фильма с id=2 не должен содержать лайк от пользователя с id=1");
    }

    @Test
    public void removeLikeForUnknownFilmTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/999/like/1"))
                .DELETE()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "DELETE /films/999/like/1 должен вернуть 404");
    }

    @Test
    public void removeLikeFromUnknownUserTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/1/like/999"))
                .DELETE()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "DELETE /films/1/like/999 должен вернуть 404");
    }

    @Test
    public void getMostPopularTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/popular?count=2"))
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /films/popular должен вернуть 200");

        String body = resp.body().trim();
        List<Film> mostPopularFilms = objectMapper.readValue(body, new TypeReference<>() {
        });

        assertEquals(2, mostPopularFilms.size(),
                "Список самых популярных фильмов должен состоять из 2 фильмов");
        assertTrue(mostPopularFilms.getFirst().getLikes().size() > mostPopularFilms.getLast().getLikes().size(),
                "Количество лайков первого фильма в списке лоджно быть больше последнего");
    }

    @Test
    public void getDefaultMostPopularTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/popular"))
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /films/popular должен вернуть 200");

        String body = resp.body().trim();
        List<Film> mostPopularFilms = objectMapper.readValue(body, new TypeReference<>() {
        });
        int maxLikes = mostPopularFilms.stream()
                .mapToInt(f -> f.getLikes().size())
                .max()
                .orElse(0);
        int minLikes = mostPopularFilms.stream()
                .mapToInt(f -> f.getLikes().size())
                .min()
                .orElse(0);

        assertTrue(mostPopularFilms.size() <= 10,
                "Список самых популярных фильмов должен состоять максимум из 10 фильмов");
        assertEquals(maxLikes, mostPopularFilms.getFirst().getLikes().size(),
                "Первый фильм в списке должен содержать максимальное количество лайков");
        assertEquals(minLikes, mostPopularFilms.getLast().getLikes().size(),
                "Последний фильм в списке должен содержать минимальное количество лайков");
    }

    @Test
    public void getMostPopularNegativeCountTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films/popular?count=-2"))
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(resp.statusCode())
                .as("Статус код ответа GET /films/popular")
                .isEqualTo(400);

        String body = resp.body().trim();
        assertThat(body)
                .as("Тело ответа")
                .isNotEmpty();

        ErrorResponse err = objectMapper.readValue(body, ErrorResponse.class);

        assertThat(err.getError())
                .as("Сообщение об ошибке")
                .contains("Количество фильмов должно быть больше 0");
    }
}
