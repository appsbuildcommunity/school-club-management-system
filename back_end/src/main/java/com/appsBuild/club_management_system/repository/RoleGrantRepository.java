package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.RoleGrant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleGrantRepository extends JpaRepository<RoleGrant, Long> {

  // Lists all provenance links for a member's grants (role links plus individual links).
  List<RoleGrant> findByGrant_ClubMembership_MembershipId(Long membershipId);

  // Lists all links pointing at the given role template (used when syncing or deleting a role).
  List<RoleGrant> findByClubRole_ClubRoleId(Long roleId);

  // Lists the links between a specific role and a specific member's grants (used when unassigning a role).
  List<RoleGrant> findByGrant_ClubMembership_MembershipIdAndClubRole_ClubRoleId(
      Long membershipId, Long roleId);

  // Returns true if the given grant row already has a link to the given role (prevents duplicate links).
  boolean existsByClubRole_ClubRoleIdAndGrant_PrivilegeId(Long roleId, Long privilegeId);

  // Deletes every link pointing at the role (called when the role itself is deleted).
  void deleteByClubRole_ClubRoleId(Long roleId);

  // Deletes the links between the member's grants and the role (called when a role is unassigned).
  void deleteByGrant_ClubMembership_MembershipIdAndClubRole_ClubRoleId(
      Long membershipId, Long roleId);
}
