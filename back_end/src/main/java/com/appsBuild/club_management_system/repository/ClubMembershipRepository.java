package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ClubMembership;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

  // Finds the membership a user has in a specific club, if it exists.
  Optional<ClubMembership> findByUser_UserIdAndClub_ClubId(Long userId, Long clubId);
}
