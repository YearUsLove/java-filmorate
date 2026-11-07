package ru.yandex.practicum.filmorate.storage;

// Storage for directors
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {

    Collection<Director> findAll();

    Optional<Director> findById(Long id);

    Director create(Director director);

    Director update(Director director);

    boolean delete(Long id);
}
