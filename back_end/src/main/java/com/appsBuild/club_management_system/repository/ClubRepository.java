package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.Club;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

  // Finds the single coordination club (is_coordination_club = true), if one has been created yet.
  Optional<Club> findByCoordinationClubTrue();
}
