package org.gupang.user.Domain.Repository;

import org.gupang.user.Domain.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.gupang.common.entity.UserRole;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Page<User> findAllByRole(UserRole role, Pageable pageable);
    Page<User> findAllByRoleAndHubId(UserRole role, UUID hubId, Pageable pageable);
    Optional<User> findById(UUID id);
    User save(User user);
    void delete(User user);
}
