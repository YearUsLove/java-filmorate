package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.base.FilmsBaseTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.practicum.filmorate.enums.RatingMPA.NC_17;

public class GetFilmsTests extends FilmsBaseTest {
    @Test
    void getFilmsListTest() throws IOException, InterruptedException {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "GET /films должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        Set<Film> films = objectMapper.readValue(body, new TypeReference<>() {});
        Film expected = baseFilm;
        expected.setId(1L);
        expected.setMpa(Rating.builder().id(5L).name(NC_17).build());
        expected.setGenres(new HashSet<>());

        assertThat(films)
                .as("Список фильмов")
                .isNotEmpty()
                .contains(expected)
                .hasSize(4);
    }
}
