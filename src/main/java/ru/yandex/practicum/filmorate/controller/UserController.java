package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation_groups.CreateSequence;
import ru.yandex.practicum.filmorate.validation_groups.UpdateSequence;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RestController
@Validated
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public Optional<User> user(@PathVariable
                               @Positive(message = "id пользователя должен быть положительным числом") Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public Collection<User> users() {
        return userService.users();
    }

    @GetMapping("/{id}/friends")
    public Collection<User> friends(@PathVariable
                                    @Positive(message = "id пользователя должен быть положительным числом") Long id) {
        return userService.friends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> mutualFriends(@PathVariable
                                          @Positive(message = "id пользователя должен быть положительным числом") Long id,
                                          @PathVariable
                                          @Positive(message = "id пользователя должен быть положительным числом") Long otherId) {
        return userService.mutualFriends(id, otherId);
    }

    @PostMapping
    public User create(@Validated(CreateSequence.class) @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Validated(UpdateSequence.class) @RequestBody User userUpdate) {
        return userService.update(userUpdate);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable
                          @Positive(message = "id пользователя должен быть положительным числом") Long id,
                          @PathVariable
                          @Positive(message = "id друга должен быть положительным числом") Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable
                             @Positive(message = "id пользователя должен быть положительным числом") Long id,
                             @PathVariable
                             @Positive(message = "id друга должен быть положительным числом") Long friendId) {
        userService.removeFriend(id, friendId);
    }
}
