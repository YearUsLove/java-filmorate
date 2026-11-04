package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> users() {
        log.info("Получен запрос на получение всех пользователей");

        Collection<User> all = userStorage.findAll();
        log.debug("Всего пользователей: {}. Список пользователей: {}", all.size(), all);
        return all;
    }

    public Optional<User> getById(Long id) {
        log.debug("Поиск пользователя по id={}", id);
        Optional<User> user = userStorage.findById(id);
        user.ifPresentOrElse(
                u -> log.debug("Пользователь {} найден по id={}", u, id),
                () -> log.debug("Пользователь id={} не найден", id)
        );

        return user;
    }

    public User create(User user) {
        log.info("Получен запрос на создание пользователя с логином '{}'", user.getLogin());
        log.debug("Данные нового пользователя: \nemail {}\n логин {}\n имя пользователя {}\nдень рождения {}",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Имя пользователя пустое, будет использован логин '{}'", user.getLogin());
            user.setName(user.getLogin());
        }
        userStorage.create(user);
        log.info("Пользователь создан: id={}, логин='{}'", user.getId(), user.getLogin());
        return user;
    }

    public User update(User userUpdate) {
        log.info("Получен запрос на обновление пользователя с id={}", userUpdate.getId());
        log.debug("Данные обновляемого пользователя: \nemail {}\n логин {}\n имя пользователя {}\nдень рождения {}",
                userUpdate.getEmail(), userUpdate.getLogin(), userUpdate.getName(), userUpdate.getBirthday());

        if (isEmpty(userUpdate.getName())) {
            log.warn("Имя пользователя пустое, будет использован логин '{}'", userUpdate.getLogin());
            userUpdate.setName(userUpdate.getLogin());
        }

        Long updatedId = userUpdate.getId();
        checkUser(updatedId, "Попытка обновить несуществующего пользователя с id={}");

        userStorage.update(userUpdate);
        log.info("Пользователь с id={} обновлен, новый логин='{}'", updatedId, userUpdate.getLogin());
        return userUpdate;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Получен запрос на добавление друга id={} пользователю id={}", friendId, userId);
        checkUser(friendId, "Попытка добавить несуществующего пользователя с id={} в друзья");
        checkUser(userId, "Попытка добавить друга несуществующему пользователю с id={}");

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} успешно добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Получен запрос на удаление друга id={} из списка друзей пользователя id={}", friendId, userId);
        checkUser(friendId, "Попытка удаления из друзей несуществующего пользователя id={}");
        checkUser(userId, "Попытка удаления из друзей у несуществующего пользователя id={}");

        boolean isRemoved = userStorage.removeFriend(userId, friendId);

        if (isRemoved) {
            log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        }
    }

    public Collection<User> friends(Long id) {
        log.info("Получен запрос на получение списка друзей пользователя с id={}", id);
        checkUser(id, "Попытка получить список друзей несуществующего пользователя с id={}");

        return userStorage.friends(id);
    }

    public Collection<User> mutualFriends(Long userId, Long otherId) {
        log.info("Получен запрос на просмотр общих друзей пользователей с id={}, {}", userId, otherId);
        checkUser(userId, "Попытка получить список общих друзей для несуществующего пользователя id={}");
        checkUser(otherId, "Попытка получить список общих друзей для несуществующего пользователя id={}");

        if (userId.equals(otherId)) {
            throw new ValidationException("Id пользователей должны отличаться для получения списка общих друзей");
        }

        return userStorage.mutualFriends(userId, otherId);
    }

    private void checkUser(Long id, String errMessage) {
        userStorage.findById(id).ifPresentOrElse(
                u -> log.debug("Пользователь {} найден по id={}", u, id),
                () -> {
                    log.error(errMessage, id);
                    throw new NotFoundException("Пользователь с id = " + id + " не найден");
                }
        );
    }
}
