package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ClubMembershipProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubMembershipProfileRepository extends JpaRepository<ClubMembershipProfile, Long> {

  // Lists all join rows for the given profile (used during sync or profile deletion).
  List<ClubMembershipProfile> findByClubProfile_ClubProfileId(Long profileId);

  // Lists all profile assignments for a membership (used when listing member privileges).
  List<ClubMembershipProfile> findByClubMembership_MembershipId(Long membershipId);

  // Returns true if the membership already holds the given profile (assign idempotency).
  boolean existsByClubMembership_MembershipIdAndClubProfile_ClubProfileId(
      Long membershipId, Long profileId);

  // Removes the join row when a profile is unassigned from a membership.
  void deleteByClubMembership_MembershipIdAndClubProfile_ClubProfileId(
      Long membershipId, Long profileId);

  // Removes all join rows pointing at the given profile (called when the profile is deleted).
  void deleteByClubProfile_ClubProfileId(Long profileId);
}
