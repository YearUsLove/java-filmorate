package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.base.UsersBaseTest;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateUsersTests extends UsersBaseTest {

    @Test
    public void updateUserTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "PUT /users должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        User respUser = objectMapper.readValue(resp.body().trim(), User.class);

        if (respUser.getFriends() != null) {
            user.setFriends(new HashMap<>(respUser.getFriends()));
        }
        assertEquals(user, respUser, "В ответе должны быть данные пользователя из запроса");
    }

    @Test
    void updateByUserWithoutIdTest() throws IOException, InterruptedException {
        User user = User.builder()
                .email("kinofil@email.ru")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("ID должен быть указан"),
                "Ответ должен содержать сообщение об ошибке 'ID должен быть указан'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithUnknownIdTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(10000L)
                .email("kinofil@email.ru")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(404, resp.statusCode(), "PUT /users должен вернуть 404");
        assertTrue(resp.body().contains("Пользователь с id = 10000 не найден"),
                "Ответ должен содержать сообщение об ошибке 'Пользователь с id = 10000 не найден'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithEmptyBodyTest() throws IOException, InterruptedException {
        User user = User.builder().build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Тело запроса не должно быть пустым"),
                "Ответ должен содержать сообщение об ошибке 'Тело запроса не должно быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithNullEmailTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Электронная почта пользователя не должна быть пустой"), "Ответ должен " +
                "содержать сообщение об ошибке 'Электронная почта пользователя не должна быть пустой'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithEmptyEmailTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Электронная почта пользователя не должна быть пустой"), "Ответ должен " +
                "содержать сообщение об ошибке 'Электронная почта пользователя не должна быть пустой'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithBlankEmailTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email(" ")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Электронная почта пользователя не должна быть пустой"), "Ответ должен " +
                "содержать сообщение об ошибке 'Электронная почта пользователя не должна быть пустой'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithEmailWithoutAtSignTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil_mail")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Почта имеет некорректный формат"),
                "Ответ должен содержать сообщение об ошибке 'Почта имеет некорректный формат'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithEmailInIncorrectFormatTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil_mail/email@")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Почта имеет некорректный формат"),
                "Ответ должен содержать сообщение об ошибке 'Почта имеет некорректный формат'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithBlankLoginTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login(" ")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Логин не может быть пустым"), "Ответ должен " +
                "содержать сообщение об ошибке 'Логин не может быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithEmptyLoginTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Логин не может быть пустым"),
                "Ответ должен содержать сообщение об ошибке 'Логин не может быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithNullLoginTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Логин не может быть пустым"),
                "Ответ должен содержать сообщение об ошибке 'Логин не может быть пустым'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithSpacesInLoginTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("kinofil 00")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Логин должен содержать только латинские буквы и цифры"), "Ответ должен " +
                "содержать сообщение об ошибке 'Логин должен содержать только латинские буквы и цифры'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithLoginInIncorrectFormatTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("($^#*(&")
                .name("Dolly")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Логин должен содержать только латинские буквы и цифры"), "Ответ должен " +
                "содержать сообщение об ошибке 'Логин должен содержать только латинские буквы и цифры'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    void updateByUserWithBirthdayAfterTodayTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("kinofil00")
                .name("Dolly")
                .birthday(LocalDate.now().plusDays(15))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Дата рождения не может быть в будущем"),
                "Ответ должен содержать сообщение об ошибке 'Дата рождения не может быть в будущем'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    public void updateByUserWithNullNameTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("kinofil00")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "PUT /users должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        User respUser = objectMapper.readValue(resp.body().trim(), User.class);
        user.setId(respUser.getId());
        user.setName(user.getLogin());

        if (respUser.getFriends() != null) {
            user.setFriends(new HashMap<>(respUser.getFriends()));
        }
        assertEquals(user, respUser, "В ответе должны быть данные пользователя из запроса с логином в имени");
    }

    @Test
    public void updateByUserWithBlankNameTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("kinofil00")
                .name("")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(200, resp.statusCode(), "PUT /users должен вернуть 200");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        User respUser = objectMapper.readValue(resp.body().trim(), User.class);
        user.setId(respUser.getId());
        user.setName(user.getLogin());

        if (respUser.getFriends() != null) {
            user.setFriends(new HashMap<>(respUser.getFriends()));
        }

        assertEquals(user, respUser, "В ответе должны быть данные пользователя из запроса с логином в имени");
    }

    @Test
    void updateByUserWithNameInIncorrectFormatTest() throws IOException, InterruptedException {
        User user = User.builder()
                .id(1L)
                .email("kinofil@email.ru")
                .login("kinofil00")
                .name("*$)_/*")
                .birthday(LocalDate.of(2000, 5, 7))
                .build();
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertEquals(400, resp.statusCode(), "PUT /users должен вернуть 400");
        assertTrue(resp.body().contains("Имя пользователя должно содержать буквы и пробелы"),
                "Ответ должен содержать сообщение об ошибке 'Имя пользователя должно содержать буквы и пробелы'");

        String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }
}
