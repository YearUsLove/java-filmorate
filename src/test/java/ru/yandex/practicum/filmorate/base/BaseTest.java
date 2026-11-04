package ru.yandex.practicum.filmorate.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static ru.yandex.practicum.filmorate.enums.RatingMPA.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(PER_CLASS)
public abstract class BaseTest {
    @LocalServerPort
    protected int port;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    @Autowired
    FilmService filmService;

    protected static HttpClient client;
    protected static List<User> testUsers = new ArrayList<>();
    protected static List<Film> testFilms = new ArrayList<>();
    protected UserStorage userStorage;
    protected FilmStorage filmStorage;
    protected Set<Genre> singleGenre = new HashSet<>();
    protected Set<Genre> fewGenres = new HashSet<>();
    protected Set<Genre> duplicateGenres = new HashSet<>();
    protected Rating rating;
    protected Set<Genre> genresPreset = new HashSet<>();
    protected Set<Rating> mpaPreset = new HashSet<>();

    @BeforeAll
    protected void beforeAll() {
        filmStorage = filmService.getFilmStorage();
        userStorage = userService.getUserStorage();
        populateGenresStorage();
        populateMpaPresetStorage();
        singleGenre.add(Genre.builder().id(4L).build());
        fewGenres.addAll(List.of(Genre.builder().id(1L).build(), Genre.builder().id(4L).build()));
        duplicateGenres.addAll(List.of(Genre.builder().id(1L).build(),
                Genre.builder().id(4L).build(), Genre.builder().id(4L).build()));
        rating = Rating.builder().id(5L).build();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    protected void createUsers() {

        for (int i = 0; i < 6; i++) {
            User user = User.builder()
                    .email("user" + i + "@email.ru")
                    .login("user1")
                    .name("Пользователь Один")
                    .birthday(LocalDate.of(2015, 8, 13))
                    .build();

            userStorage.create(user);
            testUsers.add(user);
        }
    }

    protected void createFilms() {
        storageFilm(Film.builder()
                .name("Астрал")
                .releaseDate(LocalDate.of(2011, 4, 1))
                .duration(ofMinutes(102))
                .description("Семья переезжает в новый дом, но вскоре их сын впадает в кому, а его тело становится " +
                        "порталом для злых духов из потустороннего мира.")
                .mpa(rating)
                .genres(singleGenre)
                .build());

        storageFilm(Film.builder()
                .name("Изгоняющий дьявола")
                .releaseDate(LocalDate.of(1973, 12, 26))
                .duration(ofMinutes(122))
                .description("Мать обращается за помощью к священникам, когда её дочь-подросток начинает проявлять " +
                        "жуткие признаки демонической одержимости.")
                .mpa(rating)
                .genres(singleGenre)
                .build());

        storageFilm(Film.builder()
                .name("Техасская резня бензопилой")
                .releaseDate(LocalDate.of(1974, 10, 1))
                .duration(ofMinutes(83))
                .description("Группа друзей становится жертвами семьи каннибалов в Техасе, психопата с бензопилой " +
                        "по кличке Кожаное Лицо.")
                .mpa(rating)
                .genres(singleGenre)
                .build());

        storageFilm(Film.builder()
                .name("Сияние")
                .releaseDate(LocalDate.of(1980, 5, 23))
                .duration(ofMinutes(146))
                .description("Писатель устраивается смотрителем в горный отель на зиму, где из-за изоляции и " +
                        "сверхъестественных сил его безумие приводит к насилию.")
                .mpa(rating)
                .genres(singleGenre)
                .build());
    }

    private void storageFilm(Film film) {
        filmStorage.create(film);
        testFilms.add(film);
    }

    @AfterAll
    public void tearDown() {
        filmStorage.clear();
        userStorage.clear();
    }

    private void populateGenresStorage() {
        genresPreset.add(Genre.builder().id(1L).name("Комедия").build());
        genresPreset.add(Genre.builder().id(2L).name("Драма").build());
        genresPreset.add(Genre.builder().id(3L).name("Мультфильм").build());
        genresPreset.add(Genre.builder().id(4L).name("Триллер").build());
        genresPreset.add(Genre.builder().id(5L).name("Документальный").build());
        genresPreset.add(Genre.builder().id(6L).name("Боевик").build());
    }

    private void populateMpaPresetStorage() {
        mpaPreset.add(Rating.builder().id(1L).name(G).build());
        mpaPreset.add(Rating.builder().id(2L).name(PG).build());
        mpaPreset.add(Rating.builder().id(3L).name(PG_13).build());
        mpaPreset.add(Rating.builder().id(4L).name(R).build());
        mpaPreset.add(Rating.builder().id(5L).name(NC_17).build());
    }
}
