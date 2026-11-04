package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.base.BaseTest;
import ru.yandex.practicum.filmorate.controller.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Rating;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.practicum.filmorate.enums.RatingMPA.NC_17;

public class FilmRatingTests extends BaseTest {
    @Test
    void getAllRatingsTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/mpa"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /mpa должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        Set<Rating> rates = objectMapper.readValue(body, new TypeReference<>() {
        });

        assertThat(rates).as("Полный список рейтингов")
                .isNotEmpty()
                .containsExactlyInAnyOrderElementsOf(mpaPreset);
    }

    @Test
    void getRatingByIdTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/mpa/5"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /mpa/5 должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        Rating rate = objectMapper.readValue(body, Rating.class);

        assertThat(rate).as("Рейтинг по id=5").isNotNull();
        assertThat(rate.getName()).as("Наименование рейтинга под id=5")
                .isNotNull()
                .isEqualTo(NC_17);
    }

    @Test
    void getRatingByUnknownIdTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/mpa/10000"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "GET /mpa/10000 должен вернуть 404");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        ErrorResponse err = objectMapper.readValue(body, ErrorResponse.class);

        assertThat(err).as("Сообщение об ошибке").isNotNull();
        assertThat(err.getError()).as("Сообщение о несуществующем id")
                .isNotEmpty()
                .isEqualTo("Рейтинг по id=10000 не найден");
    }
}
