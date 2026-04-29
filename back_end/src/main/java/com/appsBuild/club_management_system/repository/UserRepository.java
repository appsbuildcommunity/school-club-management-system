package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.Attachment;
import com.appsBuild.club_management_system.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
