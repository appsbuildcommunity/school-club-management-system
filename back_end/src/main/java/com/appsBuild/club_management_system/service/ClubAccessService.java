package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.entity.ClubMembershipProfile;
import com.appsBuild.club_management_system.model.entity.Endpoint;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.ClubMembershipEndpointRepository;
import com.appsBuild.club_management_system.repository.ClubMembershipProfileRepository;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EndpointRepository;
import com.appsBuild.club_management_system.repository.UserRepository;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final ClubMembershipEndpointRepository clubMembershipEndpointRepository;
  private final ClubMembershipProfileRepository clubMembershipProfileRepository;

  /** Returns the JWT from the current security context. */
  private Jwt getJwt() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new IllegalStateException("No JWT in security context");
    }
    return jwt;
  }

  /** Resolves the application user backing the current JWT, or fails with a 404. */
  public User currentUser() {
    Jwt jwt = getJwt();
    return userRepository
        .findByKeycloakSub(jwt.getSubject())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "User not found for keycloak subject: " + jwt.getSubject()));
  }

  /** True if the JWT belongs to the given user id. */
  public boolean isSelf(Long userId) {
    return currentUser().getUserId().equals(userId);
  }

  /** True if the JWT carries the realm ADMIN role. */
  public boolean isAdmin() {
    return rolesFromJwt(getJwt()).contains(ADMIN_ROLE);
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
  public boolean hasEndpoint(Long clubId, String endpointName) {
    if (isAdmin()) {
      return true;
    }
    Endpoint endpoint = resolveEndpoint(endpointName);
    if (endpoint.isPrivileged() && !clubId.equals(coordinationClubId())) {
      return false;
    }
    Optional<ClubMembership> membership =
        clubMembershipRepository.findByUser_UserIdAndClub_ClubId(
            currentUser().getUserId(), clubId);
    if (membership.isEmpty()) {
      return false;
    }
    ClubMembership clubMembership = membership.get();
    if (clubMembership.getClubRole() == ClubRole.CLUB_PRESIDENT) {
      return true;
    }
    Long mid = clubMembership.getMembershipId();
    if (clubMembershipEndpointRepository
        .existsByClubMembership_MembershipIdAndEndpoint_Name(mid, endpointName)) {
      return true;
    }
    return hasProfileEndpoint(mid, endpointName);
  }

  /** True if the caller is the coordination club president (or ADMIN). */
  public boolean isCoordinationClubPresident() {
    if (isAdmin()) {
      return true;
    }
    return isPresidentOfClub(coordinationClubId());
  }

  /** True if the caller has any membership in the given club (used by view-only endpoints). */
  public boolean hasMembership(Long clubId) {
    return clubMembershipRepository
        .findByUser_UserIdAndClub_ClubId(currentUser().getUserId(), clubId)
        .isPresent();
  }

  /** True if the caller owns the given membership. */
  public boolean isMembershipOwner(Long membershipId) {
    return clubMembershipRepository
        .findById(membershipId)
        .map(m -> m.getUser().getUserId().equals(currentUser().getUserId()))
        .orElse(false);
  }

  /** True if the caller holds at least one privilege in the given category within the club. */
  public boolean hasCategoryPrivilege(Long clubId, Category category) {
    if (isAdmin()) {
      return true;
    }
    Optional<ClubMembership> membership =
        clubMembershipRepository.findByUser_UserIdAndClub_ClubId(
            currentUser().getUserId(), clubId);
    if (membership.isEmpty()) {
      return false;
    }
    ClubMembership m = membership.get();
    if (m.getClubRole() == ClubRole.CLUB_PRESIDENT) {
      return true;
    }
    Long mid = m.getMembershipId();
    if (clubMembershipEndpointRepository
        .existsByClubMembership_MembershipIdAndEndpoint_Category(mid, category)) {
      return true;
    }
    return hasProfileCategory(mid, category);
  }

  private boolean hasProfileEndpoint(Long membershipId, String endpointName) {
    List<ClubMembershipProfile> profiles =
        clubMembershipProfileRepository.findByClubMembership_MembershipId(membershipId);
    for (ClubMembershipProfile cmp : profiles) {
      for (Endpoint ep : cmp.getClubProfile().getEndpoints()) {
        if (ep.getName().equals(endpointName)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasProfileCategory(Long membershipId, Category category) {
    List<ClubMembershipProfile> profiles =
        clubMembershipProfileRepository.findByClubMembership_MembershipId(membershipId);
    for (ClubMembershipProfile cmp : profiles) {
      for (Endpoint ep : cmp.getClubProfile().getEndpoints()) {
        if (ep.getCategory() == category) {
          return true;
        }
      }
    }
    return false;
  }

  private Endpoint resolveEndpoint(String endpointName) {
    return endpointRepository
        .findByName(endpointName)
        .orElseThrow(() -> new NotFoundException("Endpoint not found: " + endpointName));
  }

  private boolean isPresidentOfClub(Long clubId) {
    return clubMembershipRepository
        .findByUser_UserIdAndClub_ClubId(currentUser().getUserId(), clubId)
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
