package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.AssistantMemberPrivilege;
import com.appsBuild.club_management_system.model.enums.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistantMemberPrivilegeRepository extends JpaRepository<AssistantMemberPrivilege, Long> {

  boolean existsByClubMembership_MembershipIdAndEndpoint_Name(Long membershipId, String endpointName);

  void deleteByClubMembership_MembershipIdAndEndpoint_Name(Long membershipId, String endpointName);

  Optional<AssistantMemberPrivilege> findByClubMembership_MembershipIdAndEndpoint_Name(
      Long membershipId, String endpointName);

  boolean existsByClubMembership_MembershipIdAndEndpoint_Category(Long membershipId, Category category);
}
