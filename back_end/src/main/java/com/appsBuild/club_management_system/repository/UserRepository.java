package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Finds a user by their unique username, if one exists.
    Optional<User> findByUsername(String username);
    // Finds a user by their unique email, if one exists.
    Optional<User> findByEmail(String email);
    // Finds a user by their Keycloak subject ID (sub claim) from the JWT, if one exists.
    Optional<User> findByKeycloakSub(String keycloakSub);
}
