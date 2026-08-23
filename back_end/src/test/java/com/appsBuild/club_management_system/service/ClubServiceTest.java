package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.appsBuild.club_management_system.dto.club.ClubRequest;
import com.appsBuild.club_management_system.dto.club.ClubResponse;
import com.appsBuild.club_management_system.dto.club.ClubDetailResponse;
import com.appsBuild.club_management_system.dto.club.PresidentRequest;
import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import com.appsBuild.club_management_system.service.storage.S3ObjectStorageService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

  @Mock private ClubRepository clubRepository;
  @Mock private UserRepository userRepository;
  @Mock private ClubMembershipRepository clubMembershipRepository;
  @Mock private S3ObjectStorageService s3ObjectStorageService;
  @Mock private ClubAccessService clubAccessService;

  @InjectMocks private ClubService clubService;

  // ── createCoordinationClub ──────────────────────────────────────────

  @Test
  void createCoordinationClub_success() {
    when(clubRepository.findByClubName("Coordination")).thenReturn(Optional.empty());
    when(clubRepository.findByCoordinationClubTrue()).thenReturn(Optional.empty());
    when(clubRepository.save(any(Club.class)))
        .thenAnswer(
            inv -> {
              Club c = inv.getArgument(0);
              c.setClubId(1L);
              return c;
            });
    when(clubMembershipRepository.countByClub_ClubId(1L)).thenReturn(0L);

    ClubResponse resp =
        clubService.createCoordinationClub(new ClubRequest("Coordination", null, null, null));

    assertTrue(resp.isCoordinationClub());
    assertEquals(0L, resp.memberCount());
    ArgumentCaptor<Club> captor = ArgumentCaptor.forClass(Club.class);
    verify(clubRepository).save(captor.capture());
    assertTrue(captor.getValue().isCoordinationClub());
    verifyNoInteractions(userRepository);
  }

  @Test
  void createCoordinationClub_withPresident_assignsPresidency() {
    User alice = User.builder().userId(10L).username("alice").firstName("Alice").lastName("Smith").build();
    when(clubRepository.findByClubName("Coordination")).thenReturn(Optional.empty());
    when(clubRepository.findByCoordinationClubTrue()).thenReturn(Optional.empty());
    when(clubRepository.save(any(Club.class)))
        .thenAnswer(
            inv -> {
              Club c = inv.getArgument(0);
              c.setClubId(1L);
              return c;
            });
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
    when(clubMembershipRepository.save(any(ClubMembership.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(clubMembershipRepository.countByClub_ClubId(1L)).thenReturn(1L);

    clubService.createCoordinationClub(new ClubRequest("Coordination", null, null, "alice"));

    ArgumentCaptor<ClubMembership> captor = ArgumentCaptor.forClass(ClubMembership.class);
    verify(clubMembershipRepository).save(captor.capture());
    ClubMembership membership = captor.getValue();
    assertEquals(ClubRole.CLUB_PRESIDENT, membership.getClubRole());
    assertEquals(10L, membership.getUser().getUserId());
    assertTrue(membership.getClub().isCoordinationClub());
  }

  @Test
  void createCoordinationClub_alreadyExists_throwsConflict() {
    when(clubRepository.findByClubName("Coordination")).thenReturn(Optional.empty());
    Club existing = Club.builder().clubId(9L).isCoordinationClub(true).build();
    when(clubRepository.findByCoordinationClubTrue()).thenReturn(Optional.of(existing));

    assertThrows(
        ConflictException.class,
        () -> clubService.createCoordinationClub(new ClubRequest("Coordination", null, null, null)));
  }

  @Test
  void createCoordinationClub_nameTakenByRegularClub_throwsConflict() {
    Club regular = Club.builder().clubId(2L).clubName("Coordination").build();
    when(clubRepository.findByClubName("Coordination")).thenReturn(Optional.of(regular));

    assertThrows(
        ConflictException.class,
        () -> clubService.createCoordinationClub(new ClubRequest("Coordination", null, null, null)));
    verify(clubRepository, never()).findByCoordinationClubTrue();
  }

  // ── createClub ──────────────────────────────────────────────────────

  @Test
  void createClub_withPresident_assignsPresidency() {
    User alice = User.builder().userId(10L).username("alice").firstName("Alice").lastName("Smith").build();
    when(clubRepository.findByClubName("Chess Club")).thenReturn(Optional.empty());
    when(clubRepository.save(any(Club.class)))
        .thenAnswer(
            inv -> {
              Club c = inv.getArgument(0);
              c.setClubId(2L);
              return c;
            });
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
    when(clubMembershipRepository.save(any(ClubMembership.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(clubMembershipRepository.countByClub_ClubId(2L)).thenReturn(1L);

    ClubResponse resp =
        clubService.createClub(new ClubRequest("Chess Club", null, null, "alice"));

    assertEquals(1L, resp.memberCount());
    ArgumentCaptor<ClubMembership> captor = ArgumentCaptor.forClass(ClubMembership.class);
    verify(clubMembershipRepository).save(captor.capture());
    ClubMembership membership = captor.getValue();
    assertEquals(ClubRole.CLUB_PRESIDENT, membership.getClubRole());
    assertEquals(10L, membership.getUser().getUserId());
    assertNotNull(membership.getStartedAt());
  }

  @Test
  void createClub_blankPresidentUsername_isIgnored() {
    when(clubRepository.findByClubName("Chess Club")).thenReturn(Optional.empty());
    when(clubRepository.save(any(Club.class)))
        .thenAnswer(
            inv -> {
              Club c = inv.getArgument(0);
              c.setClubId(2L);
              return c;
            });
    when(clubMembershipRepository.countByClub_ClubId(2L)).thenReturn(0L);

    clubService.createClub(new ClubRequest("Chess Club", null, null, "   "));

    verifyNoInteractions(userRepository);
  }

  // ── updateClub ──────────────────────────────────────────────────────

  @Test
  void updateClub_success_renamesClub() {
    Club club = Club.builder().clubId(2L).clubName("Old").isCoordinationClub(false).build();
    when(clubRepository.findById(2L)).thenReturn(Optional.of(club));
    when(clubRepository.findByClubName("New")).thenReturn(Optional.empty());
    when(clubMembershipRepository.countByClub_ClubId(2L)).thenReturn(5L);

    ClubResponse resp = clubService.updateClub(2L, new ClubRequest("New", null, null, null));

    assertEquals("New", resp.clubName());
    assertEquals("New", club.getClubName());
    assertEquals(5L, resp.memberCount());
  }

  @Test
  void updateClub_nameTakenByOtherClub_throwsConflict() {
    Club club = Club.builder().clubId(2L).clubName("Old").build();
    Club other = Club.builder().clubId(1L).clubName("Other").build();
    when(clubRepository.findById(2L)).thenReturn(Optional.of(club));
    when(clubRepository.findByClubName("Other")).thenReturn(Optional.of(other));

    assertThrows(
        ConflictException.class,
        () -> clubService.updateClub(2L, new ClubRequest("Other", null, null, null)));
  }

  // ── deleteClub ──────────────────────────────────────────────────────

  @Test
  void deleteClub_coordinationClub_throwsConflict() {
    Club coordination = Club.builder().clubId(1L).clubName("C").isCoordinationClub(true).build();
    when(clubRepository.findById(1L)).thenReturn(Optional.of(coordination));

    assertThrows(ConflictException.class, () -> clubService.deleteClub(1L));
    verify(clubRepository, never()).delete(any(Club.class));
  }

  @Test
  void deleteClub_regular_delegatesToRepository() {
    Club club = Club.builder().clubId(2L).clubName("Chess").isCoordinationClub(false).build();
    when(clubRepository.findById(2L)).thenReturn(Optional.of(club));

    clubService.deleteClub(2L);

    verify(clubRepository).delete(club);
  }

  // ── getClubDetails ──────────────────────────────────────────────────

  @Test
  void getClubDetails_staffContainsOnlyPresidentsAndAssistants() {
    Club club = Club.builder().clubId(2L).clubName("Chess").isCoordinationClub(false).build();
    User alice = User.builder().userId(10L).username("alice").firstName("Alice").lastName("S").build();
    User bob = User.builder().userId(11L).username("bob").firstName("Bob").lastName("J").build();
    ClubMembership president =
        ClubMembership.builder().membershipId(100L).user(alice).clubRole(ClubRole.CLUB_PRESIDENT).build();
    ClubMembership assistant =
        ClubMembership.builder().membershipId(101L).user(bob).clubRole(ClubRole.ASSISTANT_MEMBER).build();

    when(clubRepository.findByClubName("Chess")).thenReturn(Optional.of(club));
    when(clubMembershipRepository.countByClub_ClubId(2L)).thenReturn(12L);
    when(clubMembershipRepository.findByClub_ClubIdAndClubRoleIn(eq(2L), anyCollection()))
        .thenReturn(List.of(president, assistant));

    ClubDetailResponse details = clubService.getClubDetails("Chess");

    assertEquals(12L, details.memberCount());
    assertEquals(2, details.staff().size());
    assertEquals(ClubRole.CLUB_PRESIDENT, details.staff().get(0).role());
    assertEquals(ClubRole.ASSISTANT_MEMBER, details.staff().get(1).role());
    verifyNoInteractions(s3ObjectStorageService);
  }

  // ── changePresident ─────────────────────────────────────────────────

  @Test
  void changePresident_demotesOldPresidentAndPromotesTarget() {
    Club club = Club.builder().clubId(2L).clubName("Chess").isCoordinationClub(false).build();
    User alice = User.builder().userId(10L).username("alice").firstName("Alice").lastName("S").build();
    User bob = User.builder().userId(11L).username("bob").firstName("Bob").lastName("J").build();
    Date termStart = new Date(0);
    ClubMembership current =
        ClubMembership.builder()
            .membershipId(100L)
            .user(alice)
            .club(club)
            .clubRole(ClubRole.CLUB_PRESIDENT)
            .startedAt(termStart)
            .build();
    ClubMembership target =
        ClubMembership.builder()
            .membershipId(101L)
            .user(bob)
            .club(club)
            .clubRole(ClubRole.MEMBER)
            .startedAt(new Date())
            .build();

    when(clubRepository.findById(2L)).thenReturn(Optional.of(club));
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
    when(clubMembershipRepository.findByUser_UserIdAndClub_ClubId(11L, 2L))
        .thenReturn(Optional.of(target));
    when(clubMembershipRepository.findByClub_ClubIdAndClubRole(2L, ClubRole.CLUB_PRESIDENT))
        .thenReturn(List.of(current));

    clubService.changePresident(2L, new PresidentRequest("bob"));

    assertEquals(ClubRole.MEMBER, current.getClubRole());
    assertEquals(ClubRole.CLUB_PRESIDENT, target.getClubRole());
  }

  @Test
  void changePresident_targetNotAMember_throwsNotFound() {
    Club club = Club.builder().clubId(2L).clubName("Chess").build();
    User bob = User.builder().userId(11L).username("bob").build();
    when(clubRepository.findById(2L)).thenReturn(Optional.of(club));
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
    when(clubMembershipRepository.findByUser_UserIdAndClub_ClubId(11L, 2L))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> clubService.changePresident(2L, new PresidentRequest("bob")));
  }

  @Test
  void changePresident_targetAlreadyPresident_throwsConflict() {
    Club club = Club.builder().clubId(2L).clubName("Chess").build();
    User alice = User.builder().userId(10L).username("alice").build();
    ClubMembership target =
        ClubMembership.builder()
            .membershipId(100L)
            .user(alice)
            .clubRole(ClubRole.CLUB_PRESIDENT)
            .build();
    when(clubRepository.findById(2L)).thenReturn(Optional.of(club));
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
    when(clubMembershipRepository.findByUser_UserIdAndClub_ClubId(10L, 2L))
        .thenReturn(Optional.of(target));

    assertThrows(
        ConflictException.class,
        () -> clubService.changePresident(2L, new PresidentRequest("alice")));
  }

  @Test
  void changePresident_coordinationClub_byNonAdmin_throwsForbidden() {
    Club coordination = Club.builder().clubId(1L).clubName("C").isCoordinationClub(true).build();
    when(clubRepository.findById(1L)).thenReturn(Optional.of(coordination));
    when(clubAccessService.isAdmin()).thenReturn(false);

    assertThrows(
        AccessDeniedException.class,
        () -> clubService.changePresident(1L, new PresidentRequest("bob")));
    verifyNoInteractions(userRepository);
  }

  @Test
  void changePresident_coordinationClub_byAdmin_succeeds() {
    Club coordination = Club.builder().clubId(1L).clubName("C").isCoordinationClub(true).build();
    User alice = User.builder().userId(10L).username("alice").build();
    User bob = User.builder().userId(11L).username("bob").build();
    Date termStart = new Date(0);
    ClubMembership current =
        ClubMembership.builder()
            .membershipId(100L)
            .user(alice)
            .club(coordination)
            .clubRole(ClubRole.CLUB_PRESIDENT)
            .startedAt(termStart)
            .build();
    ClubMembership target =
        ClubMembership.builder()
            .membershipId(101L)
            .user(bob)
            .club(coordination)
            .clubRole(ClubRole.MEMBER)
            .startedAt(new Date())
            .build();

    when(clubRepository.findById(1L)).thenReturn(Optional.of(coordination));
    when(clubAccessService.isAdmin()).thenReturn(true);
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
    when(clubMembershipRepository.findByUser_UserIdAndClub_ClubId(11L, 1L))
        .thenReturn(Optional.of(target));
    when(clubMembershipRepository.findByClub_ClubIdAndClubRole(1L, ClubRole.CLUB_PRESIDENT))
        .thenReturn(List.of(current));

    clubService.changePresident(1L, new PresidentRequest("bob"));

    assertEquals(ClubRole.MEMBER, current.getClubRole());
    assertEquals(ClubRole.CLUB_PRESIDENT, target.getClubRole());
  }
}
