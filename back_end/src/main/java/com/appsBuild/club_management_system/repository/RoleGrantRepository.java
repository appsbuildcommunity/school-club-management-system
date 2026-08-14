package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.RoleGrant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleGrantRepository extends JpaRepository<RoleGrant, Long> {

  // Lists all provenance links for a member's grants (profile links plus individual links).
  List<RoleGrant> findByGrant_ClubMembership_MembershipId(Long membershipId);

  // Lists all links pointing at the given profile (used when syncing or deleting a profile).
  List<RoleGrant> findByClubProfile_ClubProfileId(Long profileId);

  // Lists the links between a specific profile and a specific member's grants (used when unassigning a profile).
  List<RoleGrant> findByGrant_ClubMembership_MembershipIdAndClubProfile_ClubProfileId(
      Long membershipId, Long profileId);

  // Returns true if the given grant row already has a link to the given profile (prevents duplicate links).
  boolean existsByClubProfile_ClubProfileIdAndGrant_PrivilegeId(Long profileId, Long privilegeId);

  // Deletes every link pointing at the profile (called when the profile itself is deleted).
  void deleteByClubProfile_ClubProfileId(Long profileId);

  // Deletes the links between the member's grants and the profile (called when a profile is unassigned).
  void deleteByGrant_ClubMembership_MembershipIdAndClubProfile_ClubProfileId(
      Long membershipId, Long profileId);
}
