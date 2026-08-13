package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ClubRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRoleRepository extends JpaRepository<ClubRole, Long> {

  // Lists all role templates that belong to the given club.
  List<ClubRole> findByClub_ClubId(Long clubId);
}
