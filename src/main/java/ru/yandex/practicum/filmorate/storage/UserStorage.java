package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    void update(User userUpdate);

    boolean delete(Long id);

    Collection<User> findAll();

    Optional<User> findById(Long id);

    Collection<User> friends(Long id);

    void addFriend(Long userId, Long friendId);

    boolean removeFriend(Long userId, Long friendId);

    Collection<User> mutualFriends(Long userId, Long otherId);

    void clear();
}
