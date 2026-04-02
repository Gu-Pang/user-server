package org.gupang.user.Domain.Repository;

import org.gupang.user.Domain.Entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findById(UUID id);
    User save(User user);
    void delete(User user);
}
