package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.base.FilmsBaseTest;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateFilmsTests extends FilmsBaseTest {

    @Test
    public void updateFilmTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(1L)
                .name("Изгоняющий дьявола")
                .releaseDate(LocalDate.of(1973, 12, 26))
                .duration(ofMinutes(122))
                .description("Мать обращается за помощью к священникам, когда её дочь-подросток начинает проявлять " +
                        "жуткие признаки демонической одержимости.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "PUT /films должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        Film respFilm = objectMapper.readValue(resp.body().trim(), Film.class);

        if (respFilm.getLikes() != null) {
            film.setLikes(new HashSet<>(respFilm.getLikes()));
        }
        assertEquals(film, respFilm, "В ответе должны быть данные фильма из запроса");
    }

    @Test
    void updateByFilmWithEmptyBodyTest() throws IOException, InterruptedException {
        Film film = Film.builder().build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("Тело запроса не должно быть пустым"),
                "Ответ должен содержать сообщение об ошибке 'Тело запроса не должно быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithEmptyIdTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .name("Техасская резня бензопилой")
                .releaseDate(LocalDate.of(1974, 10, 1))
                .duration(ofMinutes(83))
                .description("Молодые люди в техасской глуши становятся жертвами семьи каннибалов и безумного маньяка " +
                        "с бензопилой, не оставляющего никому шанса на выживание.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("ID должен быть указан"),
                "Ответ должен содержать сообщение об ошибке 'ID должен быть указан'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithUnknownIdTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(10000L)
                .name("Техасская резня бензопилой")
                .releaseDate(LocalDate.of(1974, 10, 1))
                .duration(ofMinutes(83))
                .description("Молодые люди в техасской глуши становятся жертвами семьи каннибалов и безумного маньяка " +
                        "с бензопилой, не оставляющего никому шанса на выживание.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "PUT /films должен вернуть 404");
        assertTrue(resp.body().contains("Фильм с id = 10000 не найден"),
                "Ответ должен содержать сообщение об ошибке 'Фильм с id = 10000 не найден-'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithTooLongDescriptionTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(1L)
                .name("Сияние")
                .releaseDate(LocalDate.of(1980, 5, 23))
                .duration(ofMinutes(146))
                .description("Писатель Джек Торранс берёт семью в горный отель «Оверлук» на зимнюю сторожку. " +
                        "Снег заносит все пути, и они остаются в полном одиночестве. Вскоре Джек начинает видеть п" +
                        "ризраков прошлых постояльцев, которые подбивают его на злодеяния. Его сын Дэнни обладает " +
                        "особым даром — он чует тёмные силы отеля и видит жуткие видения. Безумие отца нарастает, " +
                        "и он решает убить жену и сына. Кошмар в замкнутом пространстве превращается в борьбу " +
                        "за выживание, где реальность и потустороннее сливаются в единый леденящий кровь ужас.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("Описание не может содержать больше 200 символов"),
                "Ответ должен содержать сообщение об ошибке 'Описание не может содержать больше 200 символов'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithReleaseDateBeforeFistFilmReleaseTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(1L)
                .name("Сияние")
                .releaseDate(LocalDate.of(1880, 5, 23))
                .duration(ofMinutes(146))
                .description("Писатель устраивается смотрителем в горный отель на зиму, где из-за изоляции и " +
                        "сверхъестественных сил его безумие приводит к насилию.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("Дата выпуска фильма не должна быть раньше 28.12.1895"),
                "Ответ должен содержать сообщение об ошибке " +
                        "'Дата выпуска фильма не должна быть раньше 28.12.1895'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithNegativeDurationTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(1L)
                .name("Сияние")
                .releaseDate(LocalDate.of(1980, 5, 23))
                .duration(ofMinutes(-146))
                .description("Писатель устраивается смотрителем в горный отель на зиму, где из-за изоляции и " +
                        "сверхъестественных сил его безумие приводит к насилию.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("Продолжительность фильма не может быть отрицательным числом"),
                "Ответ должен содержать сообщение об ошибке " +
                        "'Продолжительность фильма не может быть отрицательным числом'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithNullNameTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(1L)
                .releaseDate(LocalDate.of(2018, 6, 8))
                .duration(ofMinutes(127))
                .description("После смерти таинственной бабушки семья Грэм начинает сталкиваться с ужасающими семейными " +
                        "тайнами и зловещими силами, угрожающими их уничтожить.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("Название фильма не должно быть пустым"),
                "Ответ должен содержать сообщение об ошибке 'Название фильма не должно быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithBlankNameTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(1L)
                .name(" ")
                .releaseDate(LocalDate.of(2018, 6, 8))
                .duration(ofMinutes(127))
                .description("После смерти таинственной бабушки семья Грэм начинает сталкиваться с ужасающими семейными " +
                        "тайнами и зловещими силами, угрожающими их уничтожить.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("Название фильма не должно быть пустым"),
                "Ответ должен содержать сообщение об ошибке 'Название фильма не должно быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByFilmWithEmptyNameTest() throws IOException, InterruptedException {
        Film film = Film.builder()
                .id(1L)
                .name("")
                .releaseDate(LocalDate.of(2018, 6, 8))
                .duration(ofMinutes(127))
                .description("После смерти таинственной бабушки семья Грэм начинает сталкиваться с ужасающими семейными " +
                        "тайнами и зловещими силами, угрожающими их уничтожить.")
                .mpa(rating)
                .genres(singleGenre)
                .build();
        String jsonBody = objectMapper.writeValueAsString(film);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /films должен вернуть 400");
        assertTrue(resp.body().contains("Название фильма не должно быть пустым"),
                "Ответ должен содержать сообщение об ошибке 'Название фильма не должно быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }
}
