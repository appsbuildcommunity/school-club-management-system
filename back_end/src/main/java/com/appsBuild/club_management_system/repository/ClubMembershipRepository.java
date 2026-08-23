package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

  // Finds the membership a user has in a specific club, if it exists.
  Optional<ClubMembership> findByUser_UserIdAndClub_ClubId(Long userId, Long clubId);

  // Counts the members of a club.
  long countByClub_ClubId(Long clubId);

  // Finds the memberships of a club holding any of the given roles (staff lookup).
  List<ClubMembership> findByClub_ClubIdAndClubRoleIn(Long clubId, Collection<ClubRole> roles);

  // Finds all memberships of a club holding a specific role (e.g. current presidents).
  List<ClubMembership> findByClub_ClubIdAndClubRole(Long clubId, ClubRole role);
}
