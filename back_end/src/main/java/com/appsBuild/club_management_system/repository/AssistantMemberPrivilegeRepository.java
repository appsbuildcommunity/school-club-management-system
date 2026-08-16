package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.AssistantMemberPrivilege;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistantMemberPrivilegeRepository extends JpaRepository<AssistantMemberPrivilege, Long> {

  // Returns true if the member's effective privileges include the endpoint with the given name.
  boolean existsByClubMembership_MembershipIdAndEndpoint_Name(Long membershipId, String endpointName);

  // Deletes every grant of the named endpoint for the member (used when a grant is revoked).
  void deleteByClubMembership_MembershipIdAndEndpoint_Name(Long membershipId, String endpointName);

  // Finds the grant for a member's specific endpoint (used by revoke to inspect links before removal).
  Optional<AssistantMemberPrivilege> findByClubMembership_MembershipIdAndEndpoint_Name(
      Long membershipId, String endpointName);
}
