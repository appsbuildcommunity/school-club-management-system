package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
}
