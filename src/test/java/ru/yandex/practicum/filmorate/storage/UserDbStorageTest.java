package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    private User makeUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testCreateAndFindUserById() {
        User created = userStorage.create(makeUser("a@a.ru", "a", "A"));
        User found = userStorage.getById(created.getId());
        assertThat(found).hasFieldOrPropertyWithValue("id", created.getId());
        assertThat(found.getEmail()).isEqualTo("a@a.ru");
    }

    @Test
    void testGetAllUsers() {
        userStorage.create(makeUser("a@a.ru", "a", "A"));
        userStorage.create(makeUser("b@b.ru", "b", "B"));
        assertThat(userStorage.getAll()).hasSize(2);
    }

    @Test
    void testUpdateUser() {
        User created = userStorage.create(makeUser("a@a.ru", "a", "A"));
        created.setName("Updated");
        userStorage.update(created);
        assertThat(userStorage.getById(created.getId()).getName()).isEqualTo("Updated");
    }

    @Test
    void testDeleteUser() {
        User created = userStorage.create(makeUser("a@a.ru", "a", "A"));
        userStorage.delete(created.getId());
        assertThat(userStorage.getAll()).isEmpty();
    }

    @Test
    void testAddFriendOneWay() {
        User u1 = userStorage.create(makeUser("a@a.ru", "a", "A"));
        User u2 = userStorage.create(makeUser("b@b.ru", "b", "B"));
        userStorage.addFriend(u1.getId(), u2.getId());

        List<User> u1Friends = userStorage.getFriends(u1.getId());
        List<User> u2Friends = userStorage.getFriends(u2.getId());

        assertThat(u1Friends).hasSize(1);
        assertThat(u2Friends).isEmpty();
    }

    @Test
    void testRemoveFriend() {
        User u1 = userStorage.create(makeUser("a@a.ru", "a", "A"));
        User u2 = userStorage.create(makeUser("b@b.ru", "b", "B"));
        userStorage.addFriend(u1.getId(), u2.getId());
        userStorage.removeFriend(u1.getId(), u2.getId());
        assertThat(userStorage.getFriends(u1.getId())).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User u1 = userStorage.create(makeUser("a@a.ru", "a", "A"));
        User u2 = userStorage.create(makeUser("b@b.ru", "b", "B"));
        User u3 = userStorage.create(makeUser("c@c.ru", "c", "C"));
        userStorage.addFriend(u1.getId(), u3.getId());
        userStorage.addFriend(u2.getId(), u3.getId());

        List<User> common = userStorage.getCommonFriends(u1.getId(), u2.getId());
        assertThat(common).hasSize(1);
        assertThat(common.get(0).getId()).isEqualTo(u3.getId());
    }
}
