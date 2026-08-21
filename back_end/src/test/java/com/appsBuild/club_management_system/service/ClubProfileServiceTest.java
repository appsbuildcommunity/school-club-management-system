package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.appsBuild.club_management_system.dto.club.AssignPrivilegesRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileResponse;
import com.appsBuild.club_management_system.dto.privilege.MemberPrivilegesResponse;
import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.*;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClubProfileServiceTest {

  @Mock private ClubRepository clubRepository;
  @Mock private ClubMembershipRepository clubMembershipRepository;
  @Mock private ClubProfileRepository clubProfileRepository;
  @Mock private ClubMembershipProfileRepository clubMembershipProfileRepository;
  @Mock private ClubMembershipEndpointRepository clubMembershipEndpointRepository;
  @Mock private EndpointRepository endpointRepository;
  @Mock private ClubAccessService clubAccessService;

  @InjectMocks private ClubProfileService clubProfileService;

  private Club club;
  private Endpoint endpointA;
  private Endpoint endpointB;

  @BeforeEach
  void setUp() {
    club = Club.builder().clubId(1L).clubName("Chess Club").isCoordinationClub(false).build();
    endpointA =
        Endpoint.builder()
            .endpointId(10L)
            .name("manage_events")
            .description("Manage events")
            .category(Category.MANAGE_EVENTS)
            .privileged(false)
            .build();
    endpointB =
        Endpoint.builder()
            .endpointId(11L)
            .name("manage_posts")
            .description("Manage posts")
            .category(Category.MANAGE_POSTS)
            .privileged(false)
            .build();
  }

  // ── createProfile ───────────────────────────────────────────────────

  @Test
  void createProfile_success() {
    when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
    when(endpointRepository.findByName("manage_events")).thenReturn(Optional.of(endpointA));
    when(clubProfileRepository.save(any(ClubProfile.class)))
        .thenAnswer(
            inv -> {
              ClubProfile p = inv.getArgument(0);
              p.setClubProfileId(100L);
              return p;
            });

    ClubProfileResponse resp =
        clubProfileService.createProfile(
            1L, new ClubProfileRequest("Event Manager", List.of("manage_events")));

    assertEquals(100L, resp.profileId());
    assertEquals("Event Manager", resp.name());
    assertEquals(List.of("manage_events"), resp.endpoints());
    verify(clubProfileRepository).save(any(ClubProfile.class));
  }

  @Test
  void createProfile_blankName_throws() {
    when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

    assertThrows(
        ApplicationException.class,
        () ->
            clubProfileService.createProfile(
                1L, new ClubProfileRequest("  ", List.of())));
  }

  @Test
  void createProfile_nullName_throws() {
    when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

    assertThrows(
        ApplicationException.class,
        () ->
            clubProfileService.createProfile(
                1L, new ClubProfileRequest(null, List.of())));
  }

  @Test
  void createProfile_clubNotFound_throws() {
    when(clubRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () ->
            clubProfileService.createProfile(
                99L, new ClubProfileRequest("X", List.of())));
  }

  // ── updateProfile ───────────────────────────────────────────────────

  @Test
  void updateProfile_success() {
    ClubProfile profile =
        ClubProfile.builder()
            .clubProfileId(100L)
            .name("Old Name")
            .club(club)
            .endpoints(List.of(endpointA))
            .build();
    when(clubProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
    when(endpointRepository.findByName("manage_posts")).thenReturn(Optional.of(endpointB));

    ClubProfileResponse resp =
        clubProfileService.updateProfile(
            1L, 100L, new ClubProfileRequest("New Name", List.of("manage_posts")));

    assertEquals("New Name", resp.name());
    assertEquals(List.of("manage_posts"), resp.endpoints());
    verify(clubProfileRepository).save(profile);
  }

  // ── deleteProfile ───────────────────────────────────────────────────

  @Test
  void deleteProfile_success() {
    ClubProfile profile =
        ClubProfile.builder().clubProfileId(100L).club(club).name("P").build();
    when(clubProfileRepository.findById(100L)).thenReturn(Optional.of(profile));

    clubProfileService.deleteProfile(1L, 100L);

    verify(clubMembershipProfileRepository).deleteByClubProfile_ClubProfileId(100L);
    verify(clubProfileRepository).delete(profile);
  }

  // ── listProfiles ────────────────────────────────────────────────────

  @Test
  void listProfiles_returnsMappedList() {
    ClubProfile p1 =
        ClubProfile.builder()
            .clubProfileId(1L)
            .name("A")
            .club(club)
            .endpoints(List.of(endpointA))
            .build();
    ClubProfile p2 =
        ClubProfile.builder()
            .clubProfileId(2L)
            .name("B")
            .club(club)
            .endpoints(List.of(endpointA, endpointB))
            .build();
    when(clubProfileRepository.findByClub_ClubId(1L)).thenReturn(List.of(p1, p2));

    List<ClubProfileResponse> result = clubProfileService.listProfiles(1L);

    assertEquals(2, result.size());
    assertEquals("A", result.get(0).name());
    assertEquals(List.of("manage_events"), result.get(0).endpoints());
    assertEquals("B", result.get(1).name());
    assertEquals(2, result.get(1).endpoints().size());
  }

  // ── assignPrivileges (profile branch) ───────────────────────────────

  @Test
  void assignPrivileges_withProfile() {
    ClubProfile profile =
        ClubProfile.builder()
            .clubProfileId(100L)
            .name("P")
            .club(club)
            .endpoints(List.of(endpointA, endpointB))
            .build();
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder()
            .membershipId(50L)
            .club(club)
            .user(user)
            .clubRole(ClubRole.ASSISTANT_MEMBER)
            .build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));
    when(clubProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
    when(clubMembershipProfileRepository
            .existsByClubMembership_MembershipIdAndClubProfile_ClubProfileId(50L, 100L))
        .thenReturn(false);

    clubProfileService.assignPrivileges(
        1L, 50L, new AssignPrivilegesRequest(100L, List.of("manage_events", "manage_posts")));

    verify(clubMembershipProfileRepository).save(any(ClubMembershipProfile.class));
    verify(clubMembershipEndpointRepository, never()).save(any());
  }

  @Test
  void assignPrivileges_withProfile_alreadyAssigned_skips() {
    ClubProfile profile =
        ClubProfile.builder()
            .clubProfileId(100L)
            .name("P")
            .club(club)
            .endpoints(List.of(endpointA))
            .build();
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder()
            .membershipId(50L)
            .club(club)
            .user(user)
            .clubRole(ClubRole.ASSISTANT_MEMBER)
            .build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));
    when(clubProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
    when(clubMembershipProfileRepository
            .existsByClubMembership_MembershipIdAndClubProfile_ClubProfileId(50L, 100L))
        .thenReturn(true);

    clubProfileService.assignPrivileges(
        1L, 50L, new AssignPrivilegesRequest(100L, List.of("manage_events")));

    verify(clubMembershipProfileRepository, never()).save(any());
  }

  @Test
  void assignPrivileges_withProfile_subsetViolation_throws() {
    ClubProfile profile =
        ClubProfile.builder()
            .clubProfileId(100L)
            .name("P")
            .club(club)
            .endpoints(List.of(endpointA))
            .build();
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder()
            .membershipId(50L)
            .club(club)
            .user(user)
            .clubRole(ClubRole.ASSISTANT_MEMBER)
            .build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));
    when(clubProfileRepository.findById(100L)).thenReturn(Optional.of(profile));

    assertThrows(
        ApplicationException.class,
        () ->
            clubProfileService.assignPrivileges(
                1L, 50L, new AssignPrivilegesRequest(100L, List.of("manage_posts"))));
  }

  // ── assignPrivileges (individual branch) ────────────────────────────

  @Test
  void assignPrivileges_individual() {
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder()
            .membershipId(50L)
            .club(club)
            .user(user)
            .clubRole(ClubRole.ASSISTANT_MEMBER)
            .build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));
    when(endpointRepository.findByName("manage_events")).thenReturn(Optional.of(endpointA));
    when(clubMembershipEndpointRepository
            .existsByClubMembership_MembershipIdAndEndpoint_Name(50L, "manage_events"))
        .thenReturn(false);

    clubProfileService.assignPrivileges(
        1L, 50L, new AssignPrivilegesRequest(null, List.of("manage_events")));

    verify(clubMembershipEndpointRepository).save(any(ClubMembershipEndpoint.class));
    verify(clubMembershipProfileRepository, never()).save(any());
  }

  @Test
  void assignPrivileges_individual_alreadyGranted_skips() {
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder()
            .membershipId(50L)
            .club(club)
            .user(user)
            .clubRole(ClubRole.ASSISTANT_MEMBER)
            .build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));
    when(endpointRepository.findByName("manage_events")).thenReturn(Optional.of(endpointA));
    when(clubMembershipEndpointRepository
            .existsByClubMembership_MembershipIdAndEndpoint_Name(50L, "manage_events"))
        .thenReturn(true);

    clubProfileService.assignPrivileges(
        1L, 50L, new AssignPrivilegesRequest(null, List.of("manage_events")));

    verify(clubMembershipEndpointRepository, never()).save(any());
  }

  // ── unassignProfile ─────────────────────────────────────────────────

  @Test
  void unassignProfile_success() {
    ClubProfile profile =
        ClubProfile.builder().clubProfileId(100L).club(club).build();
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder().membershipId(50L).club(club).user(user).build();
    when(clubProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));

    clubProfileService.unassignProfile(1L, 50L, 100L);

    verify(clubMembershipProfileRepository)
        .deleteByClubMembership_MembershipIdAndClubProfile_ClubProfileId(50L, 100L);
  }

  // ── revokePrivilege ─────────────────────────────────────────────────

  @Test
  void revokePrivilege_success() {
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder().membershipId(50L).club(club).user(user).build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));

    clubProfileService.revokePrivilege(1L, 50L, "manage_events");

    verify(clubMembershipEndpointRepository)
        .deleteByClubMembership_MembershipIdAndEndpoint_Name(50L, "manage_events");
  }

  // ── getMemberPrivileges ─────────────────────────────────────────────

  @Test
  void getMemberPrivileges_mergesBothSources() {
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder().membershipId(50L).club(club).user(user).build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));

    ClubMembershipEndpoint cme =
        ClubMembershipEndpoint.builder()
            .endpoint(endpointA)
            .grantedDate(new Date())
            .build();
    when(clubMembershipEndpointRepository.findByClubMembership_MembershipId(50L))
        .thenReturn(List.of(cme));

    ClubProfile profile =
        ClubProfile.builder().clubProfileId(100L).name("P").club(club).endpoints(List.of(endpointB)).build();
    ClubMembershipProfile cmp =
        ClubMembershipProfile.builder()
            .clubMembership(membership)
            .clubProfile(profile)
            .assignedDate(new Date())
            .build();
    when(clubMembershipProfileRepository.findByClubMembership_MembershipId(50L))
        .thenReturn(List.of(cmp));

    MemberPrivilegesResponse resp = clubProfileService.getMemberPrivileges(1L, 50L);

    assertEquals(50L, resp.membershipId());
    assertEquals(2, resp.privileges().size());
  }

  @Test
  void getMemberPrivileges_deduplicatesEndpoint() {
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder().membershipId(50L).club(club).user(user).build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));

    ClubMembershipEndpoint cme =
        ClubMembershipEndpoint.builder()
            .endpoint(endpointA)
            .grantedDate(new Date())
            .build();
    when(clubMembershipEndpointRepository.findByClubMembership_MembershipId(50L))
        .thenReturn(List.of(cme));

    ClubProfile profile =
        ClubProfile.builder()
            .clubProfileId(100L)
            .name("P")
            .club(club)
            .endpoints(List.of(endpointA))
            .build();
    ClubMembershipProfile cmp =
        ClubMembershipProfile.builder()
            .clubMembership(membership)
            .clubProfile(profile)
            .assignedDate(new Date())
            .build();
    when(clubMembershipProfileRepository.findByClubMembership_MembershipId(50L))
        .thenReturn(List.of(cmp));

    MemberPrivilegesResponse resp = clubProfileService.getMemberPrivileges(1L, 50L);

    assertEquals(1, resp.privileges().size());
    assertEquals("manage_events", resp.privileges().get(0).endpointName());
  }

  // ── membership not in club ──────────────────────────────────────────

  @Test
  void assignPrivileges_membershipWrongClub_throws() {
    Club otherClub = Club.builder().clubId(2L).build();
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder().membershipId(50L).club(otherClub).user(user).build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));

    assertThrows(
        NotFoundException.class,
        () ->
            clubProfileService.assignPrivileges(
                1L, 50L, new AssignPrivilegesRequest(null, List.of("manage_events"))));
  }

  // ── role check ──────────────────────────────────────────────────────

  @Test
  void assignPrivileges_notAssistantMember_throws() {
    User user = User.builder().userId(1L).build();
    ClubMembership membership =
        ClubMembership.builder()
            .membershipId(50L)
            .club(club)
            .user(user)
            .clubRole(ClubRole.CLUB_PRESIDENT)
            .build();
    when(clubMembershipRepository.findById(50L)).thenReturn(Optional.of(membership));

    assertThrows(
        ApplicationException.class,
        () ->
            clubProfileService.assignPrivileges(
                1L, 50L, new AssignPrivilegesRequest(null, List.of("manage_events"))));
  }
}
