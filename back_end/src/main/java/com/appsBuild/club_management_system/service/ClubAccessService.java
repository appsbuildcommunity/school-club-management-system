package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.entity.Endpoint;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.AssistantMemberPrivilegeRepository;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EndpointRepository;
import com.appsBuild.club_management_system.repository.UserRepository;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Central place for club-scoped authorization checks used by {@code @PreAuthorize} via the
 * {@code clubAccess} SpEL bean.
 */
@Service("clubAccess")
@AllArgsConstructor
public class ClubAccessService {

  private static final String ADMIN_ROLE = "ADMIN";

  private final UserRepository userRepository;
  private final ClubRepository clubRepository;
  private final EndpointRepository endpointRepository;
  private final ClubMembershipRepository clubMembershipRepository;
  private final AssistantMemberPrivilegeRepository assistantMemberPrivilegeRepository;

  /** Resolves the application user backing the given JWT, or fails with a 404. */
  public User currentUser(Jwt jwt) {
    return userRepository
        .findByKeycloakSub(jwt.getSubject())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "User not found for keycloak subject: " + jwt.getSubject()));
  }

  /** True if the JWT belongs to the given user id. */
  public boolean isSelf(Jwt jwt, Long userId) {
    return currentUser(jwt).getUserId().equals(userId);
  }

  /** True if the JWT carries the realm ADMIN role. */
  public boolean isAdmin(Jwt jwt) {
    return rolesFromJwt(jwt).contains(ADMIN_ROLE);
  }

  /** Returns the coordination club's id, or fails with a 404 if it has not been created yet. */
  public Long coordinationClubId() {
    return clubRepository
        .findByCoordinationClubTrue()
        .map(Club::getClubId)
        .orElseThrow(() -> new NotFoundException("Coordination club not found"));
  }

  /**
   * True if the caller may use the given endpoint in the given club: ADMIN always wins; otherwise
   * the endpoint must be grantable in this club (privileged endpoints only in the coordination
   * club) and the caller must either be the club president or hold a grant for it.
   */
  public boolean hasEndpoint(Jwt jwt, Long clubId, String endpointName) {
    if (isAdmin(jwt)) {
      return true;
    }
    Endpoint endpoint = resolveEndpoint(endpointName);
    if (endpoint.isPrivileged() && !clubId.equals(coordinationClubId())) {
      return false;
    }
    Optional<ClubMembership> membership =
        clubMembershipRepository.findByUser_UserIdAndClub_ClubId(
            currentUser(jwt).getUserId(), clubId);
    if (membership.isEmpty()) {
      return false;
    }
    ClubMembership clubMembership = membership.get();
    if (clubMembership.getClubRole() == ClubRole.CLUB_PRESIDENT) {
      return true;
    }
    return assistantMemberPrivilegeRepository
        .existsByClubMembership_MembershipIdAndEndpoint_Name(
            clubMembership.getMembershipId(), endpointName);
  }

  /** True if the caller is the coordination club president (or ADMIN). */
  public boolean isCoordinationClubPresident(Jwt jwt) {
    if (isAdmin(jwt)) {
      return true;
    }
    return isPresidentOfClub(jwt, coordinationClubId());
  }

  /** True if the caller has any membership in the given club (used by view-only endpoints). */
  public boolean hasMembership(Jwt jwt, Long clubId) {
    return clubMembershipRepository
        .findByUser_UserIdAndClub_ClubId(currentUser(jwt).getUserId(), clubId)
        .isPresent();
  }

  private Endpoint resolveEndpoint(String endpointName) {
    return endpointRepository
        .findByName(endpointName)
        .orElseThrow(() -> new NotFoundException("Endpoint not found: " + endpointName));
  }

  private boolean isPresidentOfClub(Jwt jwt, Long clubId) {
    return clubMembershipRepository
        .findByUser_UserIdAndClub_ClubId(currentUser(jwt).getUserId(), clubId)
        .map(membership -> membership.getClubRole() == ClubRole.CLUB_PRESIDENT)
        .orElse(false);
  }

  private List<String> rolesFromJwt(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
    if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
      return List.of();
    }
    return roles.stream().map(String::valueOf).toList();
  }
}
