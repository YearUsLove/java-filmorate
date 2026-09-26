package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    @Override
    public User create(User user) {
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        User existing = users.get(user.getId());
        if (user.getFriends() == null) {
            user.setFriends(existing.getFriends());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        users.remove(id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = getById(userId);
        getById(friendId);
        // Односторонняя дружба: у user появляется friendId, у friend — нет
        user.getFriends().add(friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = getById(userId);
        getById(friendId);
        user.getFriends().remove(friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = getById(userId);
        List<User> friends = new ArrayList<>();
        for (Long id : user.getFriends()) {
            friends.add(getById(id));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getById(userId);
        User other = getById(otherId);
        Set<Long> common = new HashSet<>(user.getFriends());
        common.retainAll(other.getFriends());
        List<User> result = new ArrayList<>();
        for (Long id : common) {
            result.add(getById(id));
        }
        return result;
    }
}
