package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.warehouse.api.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
