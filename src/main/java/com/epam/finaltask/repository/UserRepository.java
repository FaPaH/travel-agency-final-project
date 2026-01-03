package com.epam.finaltask.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.epam.finaltask.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
}
