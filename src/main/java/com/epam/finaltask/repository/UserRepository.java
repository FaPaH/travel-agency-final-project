package com.epam.finaltask.repository;

import com.epam.finaltask.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);
}
