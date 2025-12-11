package com.library.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.library.domain.User;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> userStore = new HashMap<>();

    @Override
    public User save(User user) {
        userStore.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(userStore.get(id));
    }

    @Override
    public void delete(User user) {
        userStore.remove(user.getId());
    }
}