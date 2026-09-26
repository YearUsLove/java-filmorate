package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private InMemoryUserStorage userStorage;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);

        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(user1);

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        userStorage.create(user2);

        user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1992, 3, 3));
        userStorage.create(user3);
    }

    @Test
    void addFriend() {
        User updatedUser = userService.addFriend(user1.getId(), user2.getId());

        assertTrue(updatedUser.getFriends().contains(user2.getId()));
        assertTrue(userStorage.getById(user2.getId()).getFriends().contains(user1.getId()));
    }

    @Test
    void addSelfFriendShouldThrow() {
        assertThrows(ValidationException.class,
                () -> userService.addFriend(user1.getId(), user1.getId()));
    }

    @Test
    void removeFriend() {
        userService.addFriend(user1.getId(), user2.getId());
        User updatedUser = userService.removeFriend(user1.getId(), user2.getId());

        assertFalse(updatedUser.getFriends().contains(user2.getId()));
        assertFalse(userStorage.getById(user2.getId()).getFriends().contains(user1.getId()));
    }

    @Test
    void getFriends() {
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        List<User> friends = userService.getFriends(user1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(user2.getId())));
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(user3.getId())));
    }

    @Test
    void getCommonFriends() {
        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user2.getId(), user3.getId());

        List<User> common = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, common.size());
        assertEquals(user3.getId(), common.get(0).getId());
    }
}