package ru.yandex.practicum.filmorate.base;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class FriendsBaseTest extends BaseTest {

    @BeforeAll
    protected void setUp() {
        createUsers();
        addFriends();
    }

    private void addFriends() {
        userStorage.findById(1L).ifPresent(u -> userStorage.addFriend(u.getId(), 2L));
        userStorage.findById(5L).ifPresent(u -> userStorage.addFriend(u.getId(), 6L));
        userStorage.findById(5L).ifPresent(u -> userStorage.addFriend(u.getId(), 1L));
        userStorage.findById(6L).ifPresent(u -> userStorage.addFriend(u.getId(), 1L));
    }
}
