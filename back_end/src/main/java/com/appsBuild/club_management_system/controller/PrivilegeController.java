package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.annotation.GrantableEndpoint;
import com.appsBuild.club_management_system.dto.club.AssignPrivilegesRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileResponse;
import com.appsBuild.club_management_system.dto.privilege.MemberPrivilegesResponse;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.service.ClubProfileService;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs/{clubId}")
@AllArgsConstructor
public class PrivilegeController {

  private final ClubProfileService clubProfileService;

  // ── Profile CRUD ────────────────────────────────────────────────────

  @PostMapping("/profiles")
  @GrantableEndpoint(
      name = "manage_profiles",
      description = "Manage role profiles for a club",
      category = Category.MANAGE_MEMBERS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#jwt, #clubId, 'manage_profiles')")
  public ResponseEntity<ClubProfileResponse> createProfile(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long clubId,
      @RequestBody ClubProfileRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(clubProfileService.createProfile(clubId, request));
  }

  @PutMapping("/profiles/{profileId}")
  @GrantableEndpoint(
      name = "manage_profiles",
      description = "Manage role profiles for a club",
      category = Category.MANAGE_MEMBERS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#jwt, #clubId, 'manage_profiles')")
  public ResponseEntity<ClubProfileResponse> updateProfile(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long clubId,
      @PathVariable Long profileId,
      @RequestParam(defaultValue = "false") boolean sync,
      @RequestBody ClubProfileRequest request) {
    return ResponseEntity.ok(clubProfileService.updateProfile(clubId, profileId, request, sync));
  }

  @DeleteMapping("/profiles/{profileId}")
  @GrantableEndpoint(
      name = "manage_profiles",
      description = "Manage role profiles for a club",
      category = Category.MANAGE_MEMBERS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#jwt, #clubId, 'manage_profiles')")
  public ResponseEntity<Void> deleteProfile(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long clubId,
      @PathVariable Long profileId,
      @RequestParam(defaultValue = "false") boolean sync) {
    clubProfileService.deleteProfile(clubId, profileId, sync);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/profiles")
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasMembership(#jwt, #clubId)")
  public ResponseEntity<List<ClubProfileResponse>> listProfiles(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long clubId) {
    return ResponseEntity.ok(clubProfileService.listProfiles(clubId));
  }

  // ── Assignment (two branches) ───────────────────────────────────────

  @PostMapping("/members/{membershipId}/privileges")
  @GrantableEndpoint(
      name = "assign_profile",
      description = "Assign a profile or individual privileges to a member",
      category = Category.MANAGE_MEMBERS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#jwt, #clubId, 'assign_profile')")
  public ResponseEntity<Void> assignPrivileges(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long clubId,
      @PathVariable Long membershipId,
      @RequestBody AssignPrivilegesRequest request) {
    clubProfileService.assignPrivileges(clubId, membershipId, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  // ── Removal ─────────────────────────────────────────────────────────

  @DeleteMapping("/members/{membershipId}/profiles/{profileId}")
  @GrantableEndpoint(
      name = "unassign_profile",
      description = "Unassign a profile from a member",
      category = Category.MANAGE_MEMBERS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#jwt, #clubId, 'unassign_profile')")
  public ResponseEntity<Void> unassignProfile(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long clubId,
      @PathVariable Long membershipId,
      @PathVariable Long profileId) {
    clubProfileService.unassignProfile(clubId, membershipId, profileId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/members/{membershipId}/privileges/{endpointName}")
  @GrantableEndpoint(
      name = "revoke_privilege",
      description = "Revoke an individual privilege from a member",
      category = Category.MANAGE_MEMBERS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#jwt, #clubId, 'revoke_privilege')")
  public ResponseEntity<Void> revokePrivilege(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long clubId,
      @PathVariable Long membershipId,
      @PathVariable String endpointName) {
    clubProfileService.revokePrivilege(clubId, membershipId, endpointName);
    return ResponseEntity.ok().build();
  }

  // ── View ────────────────────────────────────────────────────────────

  @GetMapping("/members/{membershipId}/privileges")
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasCategoryPrivilege(#jwt, #clubId, T(com.appsBuild.club_management_system.model.enums.Category).MANAGE_MEMBERS) or @clubAccess.isMembershipOwner(#jwt, #membershipId)")
  public ResponseEntity<MemberPrivilegesResponse> getMemberPrivileges(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long clubId,
      @PathVariable Long membershipId) {
    return ResponseEntity.ok(clubProfileService.getMemberPrivileges(clubId, membershipId));
  }
}
