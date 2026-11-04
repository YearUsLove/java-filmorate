package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.base.BaseTest;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostFilmsTests extends BaseTest {
	@Test
	void postFilmTest() throws IOException, InterruptedException {
		Film film = Film.builder()
				.name("Синистер")
				.releaseDate(LocalDate.of(2012, 10, 12))
				.duration(ofMinutes(110))
				.description("Писатель находит в своем новом доме старые записи жестоких убийств и вскоре осознает, " +
						"что стал частью гораздо более зловещей истории.")
				.mpa(rating)
				.genres(singleGenre)
				.build();
		String jsonBody = objectMapper.writeValueAsString(film);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(getBaseUrl() + "/films"))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(200, resp.statusCode(), "POST /films должен вернуть 200");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");

		Film respFilm = objectMapper.readValue(resp.body().trim(), Film.class);
		film.setId(respFilm.getId());
		assertEquals(film, respFilm, "В ответе должны быть данные фильма из запроса");
	}

	@Test
	void postFilmWithTooLongDescriptionTest() throws IOException, InterruptedException {
		Film film = Film.builder()
				.name("Реинкарнация")
				.releaseDate(LocalDate.of(2018, 6, 8))
				.duration(ofMinutes(127))
				.description("После смерти эксцентричной бабушки Эллен семья Грэм погружается в мрачные тайны наследия. " +
						"Мать Энни находит в вещах покойной ритуальные символы и дневники, указывающие на культ демона " +
						"Паймона. Трагическая гибель младшей дочери запускает цепь сверхъестественных событий, " +
						"где границы реальности и безумия стираются. Давящая атмосфера, психологический ужас и " +
						"шокирующий финал делают этот фильм одним из самых жутких в современном кино.")
				.mpa(rating)
				.genres(singleGenre)
				.build();
		String jsonBody = objectMapper.writeValueAsString(film);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(getBaseUrl() + "/films"))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Описание не может содержать больше 200 символов"),
				"Ответ должен содержать сообщение об ошибке 'Описание не может содержать больше 200 символов'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithNullNameTest() throws IOException, InterruptedException {
		Film film = Film.builder()
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
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Название фильма не должно быть пустым"),
				"Ответ должен содержать сообщение об ошибке 'Название фильма не должно быть пустым'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithBlankNameTest() throws IOException, InterruptedException {
		Film film = Film.builder()
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
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Название фильма не должно быть пустым"),
				"Ответ должен содержать сообщение об ошибке 'Название фильма не должно быть пустым'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithEmptyNameTest() throws IOException, InterruptedException {
		Film film = Film.builder()
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
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Название фильма не должно быть пустым"),
				"Ответ должен содержать сообщение об ошибке 'Название фильма не должно быть пустым'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithReleaseDateBeforeFistFilmReleaseTest() throws IOException, InterruptedException {
		Film film = Film.builder()
				.name("Заклятие")
				.releaseDate(LocalDate.of(1013, 7, 19))
				.duration(ofMinutes(112))
				.description("Знаменитые охотники за привидениями Эд и Лоррейн Уоррены приходят на помощь семье, " +
						"в доме которой поселилась могущественная демоническая сущность.")
				.mpa(rating)
				.genres(singleGenre)
				.build();
		String jsonBody = objectMapper.writeValueAsString(film);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(getBaseUrl() + "/films"))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Дата выпуска фильма не должна быть раньше 28.12.1895"),
				"Ответ должен содержать сообщение об ошибке " +
						"'Дата выпуска фильма не должна быть раньше 28.12.1895'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithNegativeDurationTest() throws IOException, InterruptedException {
		Film film = Film.builder()
				.name("Заклятие")
				.releaseDate(LocalDate.of(2013, 7, 19))
				.duration(ofMinutes(-112))
				.description("Знаменитые охотники за привидениями Эд и Лоррейн Уоррены приходят на помощь семье, " +
						"в доме которой поселилась могущественная демоническая сущность.")
				.mpa(rating)
				.genres(singleGenre)
				.build();
		String jsonBody = objectMapper.writeValueAsString(film);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(getBaseUrl() + "/films"))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Продолжительность фильма не может быть отрицательным числом"),
				"Ответ должен содержать сообщение об ошибке " +
						"'Продолжительность фильма не может быть отрицательным числом'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithNotEmptyIdTest() throws IOException, InterruptedException {
		Film film = Film.builder()
				.id(1L)
				.name("Заклятие")
				.releaseDate(LocalDate.of(2013, 7, 19))
				.duration(ofMinutes(112))
				.description("Знаменитые охотники за привидениями Эд и Лоррейн Уоррены приходят на помощь семье, " +
						"в доме которой поселилась могущественная демоническая сущность.")
				.mpa(rating)
				.genres(singleGenre)
				.build();
		String jsonBody = objectMapper.writeValueAsString(film);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(getBaseUrl() + "/films"))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("ID не должен быть указан"),
				"Ответ должен содержать сообщение об ошибке 'ID не должен быть указан'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithEmptyBodyTest() throws IOException, InterruptedException {
		Film film = Film.builder().build();
		String jsonBody = objectMapper.writeValueAsString(film);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(getBaseUrl() + "/films"))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Тело запроса не должно быть пустым"),
				"Ответ должен содержать сообщение об ошибке 'Тело запроса не должно быть пустым'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}

	@Test
	void postFilmWithoutMpaTest() throws IOException, InterruptedException {
		Film film = Film.builder()
				.name("Спуск")
				.releaseDate(LocalDate.of(2005, 7, 8))
				.duration(ofMinutes(100))
				.description("Компания подруг-спелеологов оказывается запертой в неизведанной пещерной системе, " +
						"где они понимают, что в темноте обитают кровожадные монстры.")
				.genres(singleGenre)
				.build();
		String jsonBody = objectMapper.writeValueAsString(film);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(getBaseUrl() + "/films"))
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.header("Content-Type", "application/json; charset=UTF-8")
				.build();

		HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8));

		assertEquals(400, resp.statusCode(), "POST /films должен вернуть 400");
		assertTrue(resp.body().contains("Фильм должен иметь рейтинг MPAA"),
				"Ответ должен содержать сообщение об ошибке 'Фильм должен иметь рейтинг MPAA'");

		String contentTypeHeaderValue = resp.headers().firstValue("Content-Type").orElse("");
		assertEquals("application/json", contentTypeHeaderValue,
				"Content-Type должен содержать формат данных и кодировку");
	}
}
