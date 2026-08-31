package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(Long id) {
        return userStorage.getById(id);
    }

    public User createUser(User user) {
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        User existing = userStorage.getById(user.getId());
        if (user.getFriends() == null) {
            user.setFriends(existing.getFriends());
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public User addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        if (user.getFriends() != null) {
            user.getFriends().remove(friendId);
        }
        if (friend.getFriends() != null) {
            friend.getFriends().remove(userId);
        }
        return user;
    }

    public List<User> getFriends(Long userId) {
        User user = userStorage.getById(userId);
        List<User> friends = new ArrayList<>();
        if (user.getFriends() != null) {
            for (Long id : user.getFriends()) {
                friends.add(userStorage.getById(id));
            }
        }
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.getById(userId);
        User other = userStorage.getById(otherId);
        Set<Long> commonIds = new HashSet<>();
        if (user.getFriends() != null) {
            commonIds.addAll(user.getFriends());
        }
        if (other.getFriends() != null) {
            commonIds.retainAll(other.getFriends());
        }
        List<User> commonFriends = new ArrayList<>();
        for (Long id : commonIds) {
            commonFriends.add(userStorage.getById(id));
        }
        return commonFriends;
    }
}
