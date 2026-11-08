package com.appsBuild.club_management_system.service;

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
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import com.appsBuild.club_management_system.service.storage.ProfilePictureService;
import com.appsBuild.club_management_system.service.storage.S3ObjectStorageService;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private static final Set<String> ASSIGNABLE_ROLES = Set.of("USER", "STUDENT");
  private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z0-9._-]+");

  private final ClubAccessService clubAccessService;
  private final UserRepository userRepository;
  private final ClubMembershipRepository clubMembershipRepository;
  private final ProfilePictureService profilePictureService;
  private final S3ObjectStorageService storage;

  @Transactional(readOnly = true)
  public UserResponse getCurrentUser() {
    return toUserResponse(clubAccessService.currentUser());
  }

  public UserProfileResponse getUserProfile(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    return toUserProfileResponse(user);
  }

  @Transactional
  public UserResponse updateCurrentUserProfile(UpdateProfileRequest request) {
    User user = clubAccessService.currentUser();
    String username = normalizeUsername(request.username());
    String firstName = normalizeName(request.firstName(), "firstName");
    String lastName = normalizeLastName(request.lastName());

    userRepository
        .findByUsername(username)
        .filter(existing -> !existing.getUserId().equals(user.getUserId()))
        .ifPresent(
            existing -> {
              throw new ConflictException("Username is already taken");
            });

    user.setUsername(username);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setOnboardingCompleted(true);
    userRepository.save(user);

    return toUserResponse(user);
  }

  @Transactional
  public void updateRole(Long userId, String role) {
    String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    if (!ASSIGNABLE_ROLES.contains(normalizedRole)) {
      throw new ApplicationException("role must be USER or STUDENT");
    }
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    user.setRole(normalizedRole);
    userRepository.save(user);
  }

  public S3UploadResponse requestProfilePictureUpload(String filename) {
    User user = clubAccessService.currentUser();
    return profilePictureService.requestUserProfilePictureUpload(user.getUserId(), filename);
  }

  @Transactional(readOnly = true)
  public UserClubsResponse getCurrentUserClubs() {
    User user = clubAccessService.currentUser();
    List<UserClubResponse> memberships =
        clubMembershipRepository
            .findByUser_UserIdOrderByClub_ClubNameAsc(user.getUserId())
            .stream()
            .map(this::toUserClubResponse)
            .toList();
    return new UserClubsResponse(memberships);
  }

  private UserResponse toUserResponse(User user) {
    return new UserResponse(
        user.getUserId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        profilePictureUrl(user),
        List.of(user.getRole()),
        user.isOnboardingCompleted());
  }

  private UserProfileResponse toUserProfileResponse(User user) {
    return new UserProfileResponse(
        user.getUserId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        profilePictureUrl(user),
        List.of(user.getRole()));
  }

  private String normalizeUsername(String rawUsername) {
    String username = rawUsername == null ? "" : rawUsername.trim().toLowerCase(Locale.ROOT);
    if (username.length() < 3 || username.length() > 50) {
      throw new ApplicationException("username must be between 3 and 50 characters");
    }
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      throw new ApplicationException(
          "username may only contain letters, numbers, '.', '_' and '-'");
    }
    return username;
  }

  private String normalizeName(String rawName, String field) {
    String name = rawName == null ? "" : rawName.trim();
    if (name.isEmpty()) {
      throw new ApplicationException(field + " is required");
    }
    if (name.length() > 50) {
      throw new ApplicationException(field + " must be at most 50 characters");
    }
    return name;
  }

  private String normalizeLastName(String rawName) {
    String name = rawName == null ? "" : rawName.trim();
    if (name.length() > 50) {
      throw new ApplicationException("lastName must be at most 50 characters");
    }
    return name;
  }

  private UserClubResponse toUserClubResponse(ClubMembership membership) {
    Club club = membership.getClub();
    ProfilePicture picture = club.getProfilePicture();
    return new UserClubResponse(
        membership.getMembershipId(),
        club.getClubName(),
        club.getClubFullName(),
        club.getDescription(),
        club.isCoordinationClub(),
        picture == null ? null : storage.presignGetUrl(picture.getS3Key()),
        membership.getClubRole(),
        membership.getStartedAt());
  }

  private String profilePictureUrl(User user) {
    ProfilePicture picture = user.getProfilePicture();
    return picture == null ? null : storage.presignGetUrl(picture.getS3Key());
  }
}
