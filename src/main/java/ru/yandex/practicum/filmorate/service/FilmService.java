package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public Collection<Film> films() {
        log.info("Получен запрос на получение всех фильмов.");

        Collection<Film> all = filmStorage.findAll();
        log.debug("Всего фильмов: {}. Список фильмов: {}", all.size(), all);
        return all;
    }

    public Optional<Film> getById(Long id) {
        log.debug("Поиск фильма по id={}", id);
        Optional<Film> film = filmStorage.findById(id);
        film.ifPresentOrElse(
                f -> log.debug("Фильм найден: {}", f),
                () -> log.debug("Фильм с id={} не найден", id)
        );

        return film;
    }

    public Film create(Film film) {
        log.info("Получен запрос на создание фильма с названием '{}'", film.getName());
        log.debug("""
                        Данные нового фильма:
                        Название фильма {}
                        Описание фильма {}
                        Дата выпуска фильма {}
                        Продолжительность фильма {}""",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());

        filmStorage.create(film);
        log.info("Фильм создан: id={}, название='{}'", film.getId(), film.getName());
        return film;
    }

    public Film update(Film filmUpdate) {
        log.info("Получен запрос на обновление фильма с id={}", filmUpdate.getId());
        log.debug("""
                        Данные для обновления фильма:
                        Название фильма {}
                        Описание фильма {}
                        Дата выпуска фильма {}
                        Продолжительность фильма {}""",
                filmUpdate.getName(), filmUpdate.getDescription(),
                filmUpdate.getReleaseDate(), filmUpdate.getDuration());

        Long updatedId = filmUpdate.getId();
        filmStorage.findById(updatedId).ifPresentOrElse(
                f -> log.debug("Найден фильм {} по id {}", f, updatedId),
                () -> {
                    log.error("Попытка обновить несуществующий фильм с id={}", updatedId);
                    throw new NotFoundException("Фильм с id = " + updatedId + " не найден");
                }
        );

        filmStorage.update(filmUpdate);
        log.info("Фильм с id={} обновлен, новое название='{}'", updatedId, filmUpdate.getName());
        return filmUpdate;
    }

    public void like(Long filmId, Long userId) {
        log.info("Получен запрос на добавление лайка фильму с id={} от пользователя с id={}", filmId, userId);

        userService.getById(userId).ifPresentOrElse(
                u -> log.debug("Пользователь {} найден по id={}", u, userId),
                () -> {
                    log.error("Попытка поставить лайк от несуществующему пользователя с id={}", userId);
                    throw new NotFoundException("Пользователь с id = " + userId + " не найден");
                }
        );

        filmStorage.findById(filmId).ifPresentOrElse(
                f -> {
                    log.debug("Фильм {} по id={}", f, filmId);
                    filmStorage.addLike(filmId, userId);
                    log.info("Поставлен лайк фильму id={} от пользователя id={}", filmId, userId);
                },
                () -> {
                    log.error("Попытка поставить лайк несуществующему фильму с id={}", filmId);
                    throw new NotFoundException("Фильм с id = " + filmId + " не найден");
                }
        );
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Получен запрос на удаление лайка у фильма с id={} от пользователя с id={}", filmId, userId);

        userService.getById(userId).ifPresentOrElse(
                u -> log.debug("Пользователь {} найден по id={}", u, userId),
                () -> {
                    log.error("Попытка удалить лайк от несуществующего пользователя с id={}", userId);
                    throw new NotFoundException("Пользователь с id = " + userId + " не найден");
                }
        );

        filmStorage.findById(filmId).ifPresentOrElse(
                f -> {
                    log.debug("Фильм {} найден по id={}", f, filmId);
                    boolean isRemoved = filmStorage.removeLike(filmId, userId);

                    if (isRemoved) {
                        log.info("Удален лайк от пользователя id={} у фильма id={}", userId, filmId);
                    } else {
                        log.warn("Лайк от пользователя id={} у фильма id={} не найден", userId, filmId);
                    }
                },
                () -> {
                    log.error("Попытка удалить лайк у несуществующего фильма с id={}", filmId);
                    throw new NotFoundException("Фильм с id = " + filmId + " не найден");
                }
        );
    }

    public Collection<Film> getMostPopular(int count) {
        log.info("Получен запрос на получение {} самых популярных фильмов", count);

        Collection<Film> popular = filmStorage.mostPopular(count);

        log.debug("Найдено популярных фильмов: {}, из общего списка - {}", popular.size(), filmStorage.findAll().size());
        log.trace("Список фильмов: {}", filmStorage);
        log.trace("Список популярных фильмов: {}", popular);

        return popular;
    }
}
