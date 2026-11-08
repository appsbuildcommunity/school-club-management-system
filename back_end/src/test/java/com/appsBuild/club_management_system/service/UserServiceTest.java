package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.dto.user.UpdateProfileRequest;
import com.appsBuild.club_management_system.dto.user.UserClubResponse;
import com.appsBuild.club_management_system.dto.user.UserClubsResponse;
import com.appsBuild.club_management_system.dto.user.UserProfileResponse;
import com.appsBuild.club_management_system.dto.user.UserResponse;
import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import com.appsBuild.club_management_system.service.storage.ProfilePictureService;
import com.appsBuild.club_management_system.service.storage.S3ObjectStorageService;

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
class UserServiceTest {

  @Mock private ClubAccessService clubAccessService;
  @Mock private UserRepository userRepository;
  @Mock private ClubMembershipRepository clubMembershipRepository;
  @Mock private ProfilePictureService profilePictureService;
  @Mock private S3ObjectStorageService storage;

  @InjectMocks private UserService userService;

  private User user;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .userId(1L)
            .username("alice")
            .email("alice@example.com")
            .firstName("Alice")
            .lastName("Doe")
            .createdAt(new Date())
            .build();
  }

  @Test
  void getCurrentUser_withoutProfilePicture_returnsNullUrl() {
    when(clubAccessService.currentUser()).thenReturn(user);

    UserResponse result = userService.getCurrentUser();

    assertEquals(1L, result.userId());
    assertEquals("alice", result.username());
    assertEquals("alice@example.com", result.email());
    assertEquals("Alice", result.firstName());
    assertEquals("Doe", result.lastName());
    assertNull(result.profilePictureUrl());
    assertEquals(List.of("USER"), result.roles());
    assertFalse(result.onboardingCompleted());
    verifyNoInteractions(storage);
  }

  @Test
  void getCurrentUser_withProfilePicture_returnsPresignedUrl() {
    ProfilePicture picture = ProfilePicture.builder().profilePictureId(3L).s3Key("key.png").build();
    user.setProfilePicture(picture);
    user.setRole("STUDENT");
    when(clubAccessService.currentUser()).thenReturn(user);
    when(storage.presignGetUrl("key.png")).thenReturn("https://s3.example.com/key.png");

    UserResponse result = userService.getCurrentUser();

    assertEquals("https://s3.example.com/key.png", result.profilePictureUrl());
    assertEquals(List.of("STUDENT"), result.roles());
    verify(storage).presignGetUrl("key.png");
  }

  @Test
  void getUserProfile_returnsMappedProfile() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserProfileResponse result = userService.getUserProfile(1L);

    assertEquals(1L, result.userId());
    assertEquals("alice", result.username());
    assertEquals("alice@example.com", result.email());
    assertEquals("Alice", result.firstName());
    assertEquals("Doe", result.lastName());
    assertNull(result.profilePictureUrl());
    assertEquals(List.of("USER"), result.roles());
    verifyNoInteractions(storage);
  }

  @Test
  void getUserProfile_missingUser_throwsNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.getUserProfile(99L));
  }

  @Test
  void updateCurrentUserProfile_normalizesFieldsAndCompletesOnboarding() {
    when(clubAccessService.currentUser()).thenReturn(user);
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    UserResponse result =
        userService.updateCurrentUserProfile(
            new UpdateProfileRequest(" NewUser ", "  Alice  ", "  Doe  "));

    assertEquals("newuser", user.getUsername());
    assertEquals("Alice", user.getFirstName());
    assertEquals("Doe", user.getLastName());
    assertTrue(user.isOnboardingCompleted());
    assertEquals("newuser", result.username());
    assertTrue(result.onboardingCompleted());
    verify(userRepository).save(user);
  }

  @Test
  void updateCurrentUserProfile_allowsKeepingOwnUsername() {
    when(clubAccessService.currentUser()).thenReturn(user);
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

    UserResponse result =
        userService.updateCurrentUserProfile(new UpdateProfileRequest("alice", "Alice", "Doe"));

    assertEquals("alice", user.getUsername());
    assertTrue(result.onboardingCompleted());
    verify(userRepository).save(user);
  }

  @Test
  void updateCurrentUserProfile_usernameTaken_throwsConflict() {
    User other = User.builder().userId(2L).username("newuser").build();
    when(clubAccessService.currentUser()).thenReturn(user);
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(other));

    assertThrows(
        ConflictException.class,
        () ->
            userService.updateCurrentUserProfile(
                new UpdateProfileRequest("newuser", "Alice", "Doe")));

    assertEquals("alice", user.getUsername());
    assertFalse(user.isOnboardingCompleted());
    verify(userRepository, never()).save(user);
  }

  @Test
  void updateCurrentUserProfile_shortUsername_throwsApplicationException() {
    when(clubAccessService.currentUser()).thenReturn(user);

    assertThrows(
        ApplicationException.class,
        () -> userService.updateCurrentUserProfile(new UpdateProfileRequest("ab", "Alice", "Doe")));

    verifyNoInteractions(userRepository);
  }

  @Test
  void updateCurrentUserProfile_nullLastName_storesEmptyString() {
    when(clubAccessService.currentUser()).thenReturn(user);
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

    UserResponse result =
        userService.updateCurrentUserProfile(new UpdateProfileRequest("alice", "Alice", null));

    assertEquals("", user.getLastName());
    assertEquals("", result.lastName());
  }

  @Test
  void updateRole_normalizesInputAndPersists() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    userService.updateRole(1L, " student ");

    assertEquals("STUDENT", user.getRole());
    verify(userRepository).save(user);
  }

  @Test
  void updateRole_adminRole_isRejected() {
    assertThrows(ApplicationException.class, () -> userService.updateRole(1L, "ADMIN"));

    verifyNoInteractions(userRepository);
  }

  @Test
  void updateRole_unknownUser_throwsNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.updateRole(99L, "USER"));
  }

  @Test
  void requestProfilePictureUpload_delegatesForCurrentUser() {
    when(clubAccessService.currentUser()).thenReturn(user);
    S3UploadResponse response = new S3UploadResponse("https://put", "users/1/pic.png");
    when(profilePictureService.requestUserProfilePictureUpload(1L, "pic.png"))
        .thenReturn(response);

    S3UploadResponse result = userService.requestProfilePictureUpload("pic.png");

    assertEquals(response, result);
    verify(profilePictureService).requestUserProfilePictureUpload(1L, "pic.png");
  }

  @Test
  void getCurrentUserClubs_mapsMemberships() {
    Club chess =
        Club.builder()
            .clubId(2L)
            .clubName("Chess Club")
            .clubFullName("Chess Lovers")
            .description("Play chess")
            .isCoordinationClub(false)
            .profilePicture(
                ProfilePicture.builder().profilePictureId(9L).s3Key("clubs/2/pic.png").build())
            .build();
    Club coordination =
        Club.builder().clubId(1L).clubName("Coordination").isCoordinationClub(true).build();
    Date started = new Date();
    ClubMembership coordinationMembership =
        ClubMembership.builder()
            .membershipId(5L)
            .clubRole(ClubRole.CLUB_PRESIDENT)
            .startedAt(started)
            .user(user)
            .club(coordination)
            .build();
    ClubMembership chessMembership =
        ClubMembership.builder()
            .membershipId(6L)
            .clubRole(ClubRole.MEMBER)
            .startedAt(started)
            .user(user)
            .club(chess)
            .build();
    when(clubAccessService.currentUser()).thenReturn(user);
    when(clubMembershipRepository.findByUser_UserIdOrderByClub_ClubNameAsc(1L))
        .thenReturn(List.of(coordinationMembership, chessMembership));
    when(storage.presignGetUrl("clubs/2/pic.png")).thenReturn("https://s3/chess.png");

    UserClubsResponse result = userService.getCurrentUserClubs();

    assertEquals(2, result.memberships().size());

    UserClubResponse first = result.memberships().get(0);
    assertEquals(5L, first.membershipId());
    assertEquals("Coordination", first.clubName());
    assertNull(first.clubFullName());
    assertNull(first.description());
    assertTrue(first.isCoordinationClub());
    assertNull(first.profilePictureUrl());
    assertEquals(ClubRole.CLUB_PRESIDENT, first.clubRole());
    assertEquals(started, first.joinedDate());

    UserClubResponse second = result.memberships().get(1);
    assertEquals(6L, second.membershipId());
    assertEquals("Chess Club", second.clubName());
    assertEquals("Chess Lovers", second.clubFullName());
    assertEquals("Play chess", second.description());
    assertFalse(second.isCoordinationClub());
    assertEquals("https://s3/chess.png", second.profilePictureUrl());
    assertEquals(ClubRole.MEMBER, second.clubRole());
    verify(storage).presignGetUrl("clubs/2/pic.png");
  }

  @Test
  void getCurrentUserClubs_whenNoMemberships_returnsEmptyList() {
    when(clubAccessService.currentUser()).thenReturn(user);
    when(clubMembershipRepository.findByUser_UserIdOrderByClub_ClubNameAsc(1L))
        .thenReturn(List.of());

    UserClubsResponse result = userService.getCurrentUserClubs();

    assertTrue(result.memberships().isEmpty());
    verifyNoInteractions(storage);
  }
}
