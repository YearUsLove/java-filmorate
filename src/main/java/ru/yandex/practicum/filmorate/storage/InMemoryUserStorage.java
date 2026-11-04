package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.enums.FriendStatus.PENDING;

@Component
@Qualifier("inMemoryStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        Long id = IdGenerator.getNextId(users.keySet());
        user.setId(id);

        return users.put(id, user);
    }

    @Override
    public void update(User userUpdate) {
        User currentUser = users.get(userUpdate.getId());

        currentUser.setEmail(userUpdate.getEmail());
        currentUser.setLogin(userUpdate.getLogin());
        currentUser.setName(userUpdate.getName());
        currentUser.setBirthday(userUpdate.getBirthday());
    }

    @Override
    public boolean delete(Long id) {
        return users.remove(id) != null;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        return findAll().stream()
                .filter(u -> u.getId().equals(id))
                .findAny();
    }

    @Override
    public Collection<User> friends(Long id) {
        return findById(id)
                .map(u -> u.getFriends().keySet())
                .orElseGet(Collections::emptySet)
                .stream()
                .map(this::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findById(userId).ifPresent(u -> u.getFriends().put(friendId, PENDING));
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) {
        boolean friendRemoved = findById(userId).stream()
                .peek(u -> u.getFriends().remove(friendId))
                .noneMatch(u -> u.getFriends().containsKey(friendId));

        return friendRemoved &&
                findById(friendId).stream()
                        .peek(u -> u.getFriends().remove(userId))
                        .noneMatch(u -> u.getFriends().containsKey(userId));
    }

    @Override
    public Collection<User> mutualFriends(Long userId, Long otherId) {
        Set<Long> intersection = new HashSet<>(users.get(userId).getFriends().keySet());
        intersection.retainAll(users.get(otherId).getFriends().keySet());
        return intersection.stream()
                .map(users::get)
                .collect(Collectors.toSet());
    }

    @Override
    public void clear() {
        users.clear();
    }
}
