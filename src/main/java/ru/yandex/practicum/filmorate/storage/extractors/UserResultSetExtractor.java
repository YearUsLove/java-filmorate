package ru.yandex.practicum.filmorate.storage.extractors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
public class UserResultSetExtractor implements ResultSetExtractor<List<User>> {

    private static final String FRIEND_SEPARATOR = ":";

    private static final Map<String, FriendStatus> STATUS_BY_NAME = new HashMap<>();

    static {
        for (FriendStatus s : FriendStatus.values()) {
            STATUS_BY_NAME.put(s.name(), s);
        }
    }

    @Override
    public List<User> extractData(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        Set<String> unknownStatuses = new HashSet<>();

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));

            java.sql.Date birthday = rs.getDate("birthday");
            user.setBirthday(birthday != null ? birthday.toLocalDate() : null);

            Object[] friendArray = getArrayFromColumn(rs);
            if (friendArray != null) {
                for (Object obj : friendArray) {
                    if (obj == null) continue;
                    parseFriend(obj.toString(), user, unknownStatuses);
                }
            }

            users.add(user);
        }

        if (!unknownStatuses.isEmpty()) {
            log.warn("Обнаружены статусы, отсутствующие в enum FriendStatus: {}", unknownStatuses);
        }

        return users;
    }

    private void parseFriend(String raw, User user, Set<String> unknownStatuses) {
        int sepIndex = raw.indexOf(FRIEND_SEPARATOR);
        if (sepIndex <= 0) {
            log.warn("Повреждённая запись друга: '{}'", raw);
            return;
        }

        String idPart = raw.substring(0, sepIndex);
        String statusPart = raw.substring(sepIndex + FRIEND_SEPARATOR.length());

        long friendId;
        try {
            friendId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            log.warn("Не удалось разобрать id друга из '{}'", raw);
            return;
        }

        FriendStatus status = STATUS_BY_NAME.get(statusPart);
        if (status == null) {
            unknownStatuses.add(statusPart);
            return;
        }

        user.getFriends().put(friendId, status);
    }

    private Object[] getArrayFromColumn(ResultSet rs) throws SQLException {
        Object value = rs.getObject("friends");
        return switch (value) {
            case null -> null;
            case java.sql.Array array -> (Object[]) array.getArray();
            case Object[] objects -> objects;
            default -> throw new SQLException("Неожиданный тип для колонки " + "friends" + ": " + value.getClass());
        };

    }
}
