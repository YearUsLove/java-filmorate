package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testLogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createValidUser() {
        User createdUser = userController.createUser(validUser);
        assertNotNull(createdUser.getId());
        assertEquals(validUser.getEmail(), createdUser.getEmail());
        assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    void createUserWithEmptyEmail() {
        validUser.setEmail("");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void createUserWithNullEmail() {
        validUser.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void createUserWithEmailWithoutAt() {
        validUser.setEmail("invalidemail.com");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void createUserWithEmptyLogin() {
        validUser.setLogin("");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void createUserWithNullLogin() {
        validUser.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void createUserWithLoginWithSpaces() {
        validUser.setLogin("test login");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void createUserWithEmptyName() {
        validUser.setName("");
        User createdUser = userController.createUser(validUser);
        assertEquals(validUser.getLogin(), createdUser.getName());
    }

    @Test
    void createUserWithNullName() {
        validUser.setName(null);
        User createdUser = userController.createUser(validUser);
        assertEquals(validUser.getLogin(), createdUser.getName());
    }

    @Test
    void createUserWithFutureBirthday() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void createUserWithTodayBirthday() {
        validUser.setBirthday(LocalDate.now());
        User createdUser = userController.createUser(validUser);
        assertEquals(LocalDate.now(), createdUser.getBirthday());
    }

    @Test
    void updateNonExistentUser() {
        validUser.setId(999L);
        assertThrows(NotFoundException.class, () -> userController.updateUser(validUser));
    }

    @Test
    void updateExistingUser() {
        userController.createUser(validUser);

        User updatedUser = new User();
        updatedUser.setId(validUser.getId());
        updatedUser.setEmail(validUser.getEmail());
        updatedUser.setLogin(validUser.getLogin());
        updatedUser.setName("Обновленное имя");
        updatedUser.setBirthday(validUser.getBirthday());

        User result = userController.updateUser(updatedUser);
        assertEquals("Обновленное имя", result.getName());
    }
}