package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ClubMembershipEndpoint;
import com.appsBuild.club_management_system.model.enums.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubMembershipEndpointRepository extends JpaRepository<ClubMembershipEndpoint, Long> {

  boolean existsByClubMembership_MembershipIdAndEndpoint_Name(
      Long membershipId, String endpointName);

  void deleteByClubMembership_MembershipIdAndEndpoint_Name(
      Long membershipId, String endpointName);

  Optional<ClubMembershipEndpoint> findByClubMembership_MembershipIdAndEndpoint_Name(
      Long membershipId, String endpointName);

  List<ClubMembershipEndpoint> findByClubMembership_MembershipId(Long membershipId);

  boolean existsByClubMembership_MembershipIdAndEndpoint_Category(
      Long membershipId, Category category);
}
