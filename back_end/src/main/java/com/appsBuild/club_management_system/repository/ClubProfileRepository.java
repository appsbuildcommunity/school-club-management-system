package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ClubProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubProfileRepository extends JpaRepository<ClubProfile, Long> {

  // Lists all role templates (profiles) that belong to the given club.
  List<ClubProfile> findByClub_ClubId(Long clubId);
}
