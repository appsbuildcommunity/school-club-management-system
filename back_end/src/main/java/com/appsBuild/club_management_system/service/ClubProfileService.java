package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.dto.club.AssignPrivilegesRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileResponse;
import com.appsBuild.club_management_system.dto.privilege.MemberPrivilege;
import com.appsBuild.club_management_system.dto.privilege.MemberPrivilegesResponse;
import com.appsBuild.club_management_system.dto.privilege.SourceProfile;
import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.AssistantMemberPrivilege;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.entity.ClubMembershipProfile;
import com.appsBuild.club_management_system.model.entity.ClubProfile;
import com.appsBuild.club_management_system.model.entity.Endpoint;
import com.appsBuild.club_management_system.model.entity.RoleGrant;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.AssistantMemberPrivilegeRepository;
import com.appsBuild.club_management_system.repository.ClubMembershipProfileRepository;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.ClubProfileRepository;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EndpointRepository;
import com.appsBuild.club_management_system.repository.RoleGrantRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ClubProfileService {

  private final ClubRepository clubRepository;
  private final ClubMembershipRepository clubMembershipRepository;
  private final ClubProfileRepository clubProfileRepository;
  private final ClubMembershipProfileRepository clubMembershipProfileRepository;
  private final EndpointRepository endpointRepository;
  private final AssistantMemberPrivilegeRepository assistantMemberPrivilegeRepository;
  private final RoleGrantRepository roleGrantRepository;
  private final ClubAccessService clubAccessService;

  // ── Profile CRUD ────────────────────────────────────────────────────

  @Transactional
  public ClubProfileResponse createProfile(Long clubId, ClubProfileRequest request) {
    Club club = resolveClub(clubId);
    validateRequest(request, club);
    ClubProfile profile = clubProfileRepository.save(
        ClubProfile.builder()
            .name(request.name())
            .createdAt(new Date())
            .club(club)
            .endpoints(resolveEndpoints(request.endpoints()))
            .build());
    return toProfileResponse(profile);
  }

  @Transactional
  public ClubProfileResponse updateProfile(
      Long clubId, Long profileId, ClubProfileRequest request, boolean sync) {
    ClubProfile profile = resolveProfileInClub(clubId, profileId);
    Club club = profile.getClub();
    validateRequest(request, club);
    List<Endpoint> updatedEndpoints = resolveEndpoints(request.endpoints());
    profile.setName(request.name());
    profile.setEndpoints(updatedEndpoints);
    clubProfileRepository.save(profile);
    if (sync) {
      propagateToHolders(profile, updatedEndpoints);
    }
    return toProfileResponse(profile);
  }

  @Transactional
  public void deleteProfile(Long clubId, Long profileId, boolean sync) {
    ClubProfile profile = resolveProfileInClub(clubId, profileId);
    List<RoleGrant> links = roleGrantRepository.findByClubProfile_ClubProfileId(profileId);
    Set<Long> grantIds =
        links.stream().map(link -> link.getGrant().getPrivilegeId()).collect(Collectors.toSet());
    roleGrantRepository.deleteByClubProfile_ClubProfileId(profileId);
    if (sync) {
      grantIds.forEach(this::deleteIfOrphan);
    } else {
      grantIds.forEach(this::reSourceIndividual);
    }
    clubMembershipProfileRepository.deleteByClubProfile_ClubProfileId(profileId);
    clubProfileRepository.delete(profile);
  }

  @Transactional(readOnly = true)
  public List<ClubProfileResponse> listProfiles(Long clubId) {
    return clubProfileRepository.findByClub_ClubId(clubId).stream()
        .map(this::toProfileResponse)
        .toList();
  }

  // ── Assignment (two branches) ───────────────────────────────────────

  @Transactional
  public void assignPrivileges(
      Long clubId, Long membershipId, AssignPrivilegesRequest request) {
    ClubMembership membership = resolveMembershipInClub(clubId, membershipId);
    if (membership.getClubRole() != ClubRole.ASSISTANT_MEMBER) {
      throw new ApplicationException("Only assistant members can be granted privileges");
    }
    if (request.profileId() != null) {
      ClubProfile profile = resolveProfileInClub(clubId, request.profileId());
      requireSameClub(profile.getClub(), membership.getClub());
      validateSubset(request.endpoints(), profile);
      ensureProfileAssignment(membership, profile);
      for (String endpointName : request.endpoints()) {
        Endpoint endpoint = grantableEndpoint(endpointName, clubId);
        ensureGrant(membership, endpoint, profile);
      }
    } else {
      for (String endpointName : request.endpoints()) {
        Endpoint endpoint = grantableEndpoint(endpointName, clubId);
        ensureGrant(membership, endpoint, null);
      }
    }
  }

  // ── Removal ─────────────────────────────────────────────────────────

  @Transactional
  public void unassignProfile(Long clubId, Long membershipId, Long profileId) {
    resolveProfileInClub(clubId, profileId);
    resolveMembershipInClub(clubId, membershipId);
    List<RoleGrant> links = roleGrantRepository
        .findByGrant_ClubMembership_MembershipIdAndClubProfile_ClubProfileId(
            membershipId, profileId);
    Set<Long> grantIds =
        links.stream().map(link -> link.getGrant().getPrivilegeId()).collect(Collectors.toSet());
    roleGrantRepository.deleteByGrant_ClubMembership_MembershipIdAndClubProfile_ClubProfileId(
        membershipId, profileId);
    grantIds.forEach(this::deleteIfOrphan);
    clubMembershipProfileRepository
        .deleteByClubMembership_MembershipIdAndClubProfile_ClubProfileId(membershipId, profileId);
  }

  @Transactional
  public void revokePrivilege(Long clubId, Long membershipId, String endpointName) {
    resolveMembershipInClub(clubId, membershipId);
    assistantMemberPrivilegeRepository
        .findByClubMembership_MembershipIdAndEndpoint_Name(membershipId, endpointName)
        .ifPresent(
            grant -> {
              List<RoleGrant> links =
                  roleGrantRepository.findByGrant_PrivilegeId(grant.getPrivilegeId());
              links.stream()
                  .filter(link -> link.getClubProfile() == null)
                  .forEach(roleGrantRepository::delete);
              deleteIfOrphan(grant.getPrivilegeId());
            });
  }

  // ── View ────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public MemberPrivilegesResponse getMemberPrivileges(Long clubId, Long membershipId) {
    resolveMembershipInClub(clubId, membershipId);
    Map<Long, MemberPrivilege> byGrant = new LinkedHashMap<>();
    for (RoleGrant link :
        roleGrantRepository.findByGrant_ClubMembership_MembershipId(membershipId)) {
      AssistantMemberPrivilege grant = link.getGrant();
      SourceProfile source =
          link.getClubProfile() == null
              ? new SourceProfile(null, null)
              : new SourceProfile(
                  link.getClubProfile().getClubProfileId(),
                  link.getClubProfile().getName());
      byGrant
          .computeIfAbsent(
              grant.getPrivilegeId(),
              id ->
                  new MemberPrivilege(
                      id,
                      grant.getEndpoint().getName(),
                      grant.getGrantedDate(),
                      new ArrayList<>()))
          .sources()
          .add(source);
    }
    return new MemberPrivilegesResponse(membershipId, new ArrayList<>(byGrant.values()));
  }

  // ── Helpers ─────────────────────────────────────────────────────────

  private void propagateToHolders(ClubProfile profile, List<Endpoint> updatedEndpoints) {
    for (ClubMembershipProfile holder :
        clubMembershipProfileRepository.findByClubProfile_ClubProfileId(
            profile.getClubProfileId())) {
      ClubMembership m = holder.getClubMembership();
      for (Endpoint endpoint : updatedEndpoints) {
        ensureGrant(m, endpoint, profile);
      }
      for (RoleGrant link :
          roleGrantRepository.findByGrant_ClubMembership_MembershipIdAndClubProfile_ClubProfileId(
              m.getMembershipId(), profile.getClubProfileId())) {
        if (updatedEndpoints.stream()
            .noneMatch(
                e -> e.getEndpointId().equals(link.getGrant().getEndpoint().getEndpointId()))) {
          deleteLinkAndOrphan(link);
        }
      }
    }
  }

  private void ensureProfileAssignment(ClubMembership membership, ClubProfile profile) {
    if (!clubMembershipProfileRepository
        .existsByClubMembership_MembershipIdAndClubProfile_ClubProfileId(
            membership.getMembershipId(), profile.getClubProfileId())) {
      clubMembershipProfileRepository.save(
          ClubMembershipProfile.builder()
              .clubMembership(membership)
              .clubProfile(profile)
              .assignedDate(new Date())
              .build());
    }
  }

  private void ensureGrant(ClubMembership membership, Endpoint endpoint, ClubProfile profile) {
    AssistantMemberPrivilege grant =
        assistantMemberPrivilegeRepository
            .findByClubMembership_MembershipIdAndEndpoint_Name(
                membership.getMembershipId(), endpoint.getName())
            .orElseGet(
                () ->
                    assistantMemberPrivilegeRepository.save(
                        AssistantMemberPrivilege.builder()
                            .clubMembership(membership)
                            .endpoint(endpoint)
                            .grantedDate(new Date())
                            .build()));
    boolean linked =
        profile == null
            ? roleGrantRepository.existsByGrant_PrivilegeIdAndClubProfileIsNull(
                grant.getPrivilegeId())
            : roleGrantRepository.existsByClubProfile_ClubProfileIdAndGrant_PrivilegeId(
                profile.getClubProfileId(), grant.getPrivilegeId());
    if (!linked) {
      roleGrantRepository.save(
          RoleGrant.builder().grant(grant).clubProfile(profile).build());
    }
  }

  private void deleteLinkAndOrphan(RoleGrant link) {
    Long grantId = link.getGrant().getPrivilegeId();
    roleGrantRepository.delete(link);
    deleteIfOrphan(grantId);
  }

  private void deleteIfOrphan(Long grantId) {
    if (roleGrantRepository.findByGrant_PrivilegeId(grantId).isEmpty()) {
      assistantMemberPrivilegeRepository.deleteById(grantId);
    }
  }

  private void reSourceIndividual(Long grantId) {
    List<RoleGrant> remaining = roleGrantRepository.findByGrant_PrivilegeId(grantId);
    boolean alreadyIndividual =
        remaining.stream().anyMatch(link -> link.getClubProfile() == null);
    if (remaining.isEmpty() && !alreadyIndividual) {
      AssistantMemberPrivilege grant =
          assistantMemberPrivilegeRepository.findById(grantId).orElse(null);
      if (grant != null) {
        roleGrantRepository.save(
            RoleGrant.builder().grant(grant).clubProfile(null).build());
      }
    }
  }

  private void validateRequest(ClubProfileRequest request, Club club) {
    if (request.name() == null || request.name().isBlank()) {
      throw new ApplicationException("name is required");
    }
    resolveEndpoints(request.endpoints()).forEach(e -> grantableEndpoint(e.getName(), club.getClubId()));
  }

  private void validateSubset(List<String> requestedEndpoints, ClubProfile profile) {
    Set<String> templateEndpoints =
        profile.getEndpoints().stream().map(Endpoint::getName).collect(Collectors.toSet());
    for (String name : requestedEndpoints) {
      if (!templateEndpoints.contains(name)) {
        throw new ApplicationException(
            "Endpoint '" + name + "' is not part of the profile's template");
      }
    }
  }

  private Endpoint grantableEndpoint(String name, Long clubId) {
    Endpoint endpoint =
        endpointRepository
            .findByName(name)
            .orElseThrow(() -> new NotFoundException("Endpoint not found: " + name));
    if (endpoint.isPrivileged() && !clubId.equals(clubAccessService.coordinationClubId())) {
      throw new AccessDeniedException(
          "Privileged endpoint can only be granted in the coordination club");
    }
    return endpoint;
  }

  private List<Endpoint> resolveEndpoints(List<String> names) {
    if (names == null || names.isEmpty()) {
      return List.of();
    }
    return names.stream()
        .distinct()
        .map(
            n ->
                endpointRepository
                    .findByName(n)
                    .orElseThrow(() -> new NotFoundException("Endpoint not found: " + n)))
        .toList();
  }

  private Club resolveClub(Long clubId) {
    return clubRepository
        .findById(clubId)
        .orElseThrow(() -> new NotFoundException("Club not found: " + clubId));
  }

  private ClubProfile resolveProfileInClub(Long clubId, Long profileId) {
    ClubProfile profile =
        clubProfileRepository
            .findById(profileId)
            .orElseThrow(() -> new NotFoundException("Profile not found: " + profileId));
    if (!profile.getClub().getClubId().equals(clubId)) {
      throw new NotFoundException("Profile not found");
    }
    return profile;
  }

  private ClubMembership resolveMembershipInClub(Long clubId, Long membershipId) {
    ClubMembership membership =
        clubMembershipRepository
            .findById(membershipId)
            .orElseThrow(() -> new NotFoundException("Membership not found: " + membershipId));
    if (!membership.getClub().getClubId().equals(clubId)) {
      throw new NotFoundException("Membership not found");
    }
    return membership;
  }

  private void requireSameClub(Club a, Club b) {
    if (!a.getClubId().equals(b.getClubId())) {
      throw new ApplicationException("Profile belongs to a different club");
    }
  }

  private ClubProfileResponse toProfileResponse(ClubProfile p) {
    return new ClubProfileResponse(
        p.getClubProfileId(),
        p.getName(),
        p.getEndpoints().stream().map(Endpoint::getName).toList());
  }
}
