package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;

@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }
        user.setFriends(new HashSet<>());
        return user;
    };

    @Override
    public List<User> getAll() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users ORDER BY id", USER_MAPPER);
        for (User user : users) {
            loadFriends(user);
        }
        return users;
    }

    @Override
    public User getById(Long id) {
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users WHERE id = ?", USER_MAPPER, id);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        User user = users.get(0);
        loadFriends(user);
        return user;
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        int updated = jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        if (updated == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return getById(user.getId());
    }

    @Override
    public void delete(Long id) {
        int deleted = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        if (deleted == 0) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!existsById(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        jdbcTemplate.update(
                "MERGE INTO friendships (user_id, friend_id, status) KEY (user_id, friend_id) " +
                        "VALUES (?, ?, 'UNCONFIRMED')",
                userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!existsById(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        List<User> friends = jdbcTemplate.query(
                "SELECT u.* FROM users u " +
                        "JOIN friendships f ON u.id = f.friend_id " +
                        "WHERE f.user_id = ? ORDER BY u.id",
                USER_MAPPER, userId);
        for (User u : friends) {
            loadFriends(u);
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!existsById(otherId)) {
            throw new NotFoundException("Пользователь с id=" + otherId + " не найден");
        }
        List<User> users = jdbcTemplate.query(
                "SELECT u.* FROM users u " +
                        "JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                        "JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = ? " +
                        "ORDER BY u.id",
                USER_MAPPER, userId, otherId);
        for (User u : users) {
            loadFriends(u);
        }
        return users;
    }

    private void loadFriends(User user) {
        List<Long> friends = jdbcTemplate.query(
                "SELECT friend_id FROM friendships WHERE user_id = ?",
                (rs, rowNum) -> rs.getLong("friend_id"),
                user.getId());
        user.setFriends(new HashSet<>(friends));
    }
}
