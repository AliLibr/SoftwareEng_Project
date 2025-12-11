package com.library.repository;

import java.util.Optional;
import com.library.domain.User;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    void delete(User user);
}