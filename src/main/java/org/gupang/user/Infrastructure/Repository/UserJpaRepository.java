package org.gupang.user.Infrastructure.Repository;

import org.gupang.user.Domain.Entity.User;
import org.gupang.user.Domain.Repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID>, UserRepository {
    @Override
    Optional<User> findByUsername(String username);
}
