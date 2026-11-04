package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.extractors.UserResultSetExtractor;

import javax.sql.DataSource;
import java.util.*;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String FIND_ALL_SQL = """
            SELECT u.*,
                   f.friends AS friends
            FROM users u
            LEFT JOIN (
                SELECT fr.user_id,
                       array_agg(DISTINCT fr.friend_id || ':' || fs.status) AS friends
                FROM friends fr
                JOIN friend_status fs ON fs.id = fr.status_id
                GROUP BY fr.user_id
            ) f ON f.user_id = u.id
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT u.*,
                   f.friends AS friends
            FROM users u
            LEFT JOIN (
                SELECT fr.user_id,
                       array_agg(DISTINCT fr.friend_id || ':' || fs.status) AS friends
                FROM friends fr
                JOIN friend_status fs ON fs.id = fr.status_id
                WHERE fr.user_id = :id
                GROUP BY fr.user_id
            ) f ON f.user_id = u.id
            WHERE u.id = :id
            """;

    private static final String UPDATE_USER_SQL = """
            UPDATE users
            SET email    = :email,
                login    = :login,
                name     = :name,
                birthday = :birthday
            WHERE id = :id
            """;

    private static final String DELETE_USER_SQL = """
            DELETE FROM users WHERE id = :id
            """;

    private static final String FIND_FRIENDS_SQL = """
            SELECT u.*,
                   f.friends AS friends
            FROM users u
            JOIN friends fr ON fr.friend_id = u.id
            LEFT JOIN (
                SELECT fr2.user_id,
                       array_agg(DISTINCT fr2.friend_id || ':' || fs.status) AS friends
                FROM friends fr2
                JOIN friend_status fs ON fs.id = fr2.status_id
                GROUP BY fr2.user_id
            ) f ON f.user_id = u.id
            WHERE fr.user_id = :userId
            """;

    private static final String INSERT_FRIEND_SQL = """
            INSERT INTO friends (user_id, friend_id, status_id)
            VALUES (:userId, :friendId, (SELECT id FROM friend_status WHERE status = 'PENDING'))
            """;

    private static final String DELETE_FRIEND_SQL = """
            DELETE FROM friends
            WHERE (user_id = :userId AND friend_id = :friendId)
               OR (user_id = :friendId AND friend_id = :userId
                   AND EXISTS (
                       SELECT 1 FROM friends f2
                       WHERE f2.user_id = :userId
                   ))
            """;

    private static final String MUTUAL_FRIENDS_SQL = """
            SELECT u.*,
                   f.friends AS friends
            FROM users u
            JOIN friends f1 ON f1.friend_id = u.id AND f1.user_id = :userId
            JOIN friends f2 ON f2.friend_id = u.id AND f2.user_id = :otherId
            LEFT JOIN (
                SELECT fr.user_id,
                       array_agg(DISTINCT fr.friend_id || ':' || fs.status) AS friends
                FROM friends fr
                JOIN friend_status fs ON fs.id = fr.status_id
                GROUP BY fr.user_id
            ) f ON f.user_id = u.id
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserResultSetExtractor extractor;
    private final DataSource dataSource;

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("ID");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("EMAIL", user.getEmail());
        parameters.put("LOGIN", user.getLogin());
        parameters.put("NAME", user.getName());
        parameters.put("BIRTHDAY", user.getBirthday());

        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        if (id == 0) {
            throw new RuntimeException("Пользователь не был создан по неизвестной причине");
        }

        user.setId(id);
        return user;
    }

    @Override
    public void update(User userUpdate) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(userUpdate);
        int updated = namedParameterJdbcTemplate.update(UPDATE_USER_SQL, params);

        if (updated == 0) {
            throw new RuntimeException("Не удалось обновить пользователя id=" + userUpdate.getId());
        }
    }

    @Override
    public boolean delete(Long id) {
        int deleted = namedParameterJdbcTemplate.update(
                DELETE_USER_SQL, new MapSqlParameterSource("id", id));

        return deleted > 0;
    }

    @Override
    public Collection<User> findAll() {
        return namedParameterJdbcTemplate.query(FIND_ALL_SQL, extractor);
    }

    @Override
    public Optional<User> findById(Long id) {
        List<User> users = namedParameterJdbcTemplate.query(
                FIND_BY_ID_SQL,
                new MapSqlParameterSource("id", id),
                extractor);

        return Optional.ofNullable(users)
                .filter(list -> !list.isEmpty())
                .map(List::getFirst);
    }

    @Override
    public Collection<User> friends(Long id) {
        return namedParameterJdbcTemplate.query(
                FIND_FRIENDS_SQL,
                new MapSqlParameterSource("userId", id),
                extractor);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        namedParameterJdbcTemplate.update(INSERT_FRIEND_SQL, params);
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        int deleted = namedParameterJdbcTemplate.update(DELETE_FRIEND_SQL, params);
        return deleted > 0;
    }

    @Override
    public Collection<User> mutualFriends(Long userId, Long otherId) {
        return namedParameterJdbcTemplate.query(
                MUTUAL_FRIENDS_SQL,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("otherId", otherId),
                extractor);
    }

    @Override
    public void clear() {

    }
}
