package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.dto.club.ClubDetailResponse;
import com.appsBuild.club_management_system.dto.club.ClubRequest;
import com.appsBuild.club_management_system.dto.club.ClubResponse;
import com.appsBuild.club_management_system.dto.club.MemberResponse;
import com.appsBuild.club_management_system.dto.club.PresidentRequest;
import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import com.appsBuild.club_management_system.service.storage.S3ObjectStorageService;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ClubService {

  /** Roles considered club staff (exposed in the detail view). */
  private static final List<ClubRole> STAFF_ROLES =
      List.of(ClubRole.CLUB_PRESIDENT, ClubRole.ASSISTANT_MEMBER);

  private final ClubRepository clubRepository;
  private final UserRepository userRepository;
  private final ClubMembershipRepository clubMembershipRepository;
  private final S3ObjectStorageService s3ObjectStorageService;
  private final ClubAccessService clubAccessService;

  // ── Create ──────────────────────────────────────────────────────────

  /**
   * Creates the single coordination club. ADMIN-only (enforced at controller). Optionally
   * assigns its first president, like any regular club.
   */
  @Transactional
  public ClubResponse createCoordinationClub(ClubRequest request) {
    ensureNameAvailable(request.clubName(), null);
    if (clubRepository.findByCoordinationClubTrue().isPresent()) {
      throw new ConflictException("Coordination club already exists");
    }
    return createAndOptionallyAssignPresident(request, true);
  }

  /** Creates a regular club, optionally assigning its first president. */
  @Transactional
  public ClubResponse createClub(ClubRequest request) {
    ensureNameAvailable(request.clubName(), null);
    return createAndOptionallyAssignPresident(request, false);
  }

  private ClubResponse createAndOptionallyAssignPresident(
      ClubRequest request, boolean isCoordinationClub) {
    Club club =
        clubRepository.save(
            Club.builder()
                .clubName(request.clubName())
                .clubFullName(request.clubFullName())
                .description(request.description())
                .isCoordinationClub(isCoordinationClub)
                .build());
    if (request.presidentUsername() != null && !request.presidentUsername().isBlank()) {
      assignPresident(club, resolveUser(request.presidentUsername()));
    }
    return toResponse(club);
  }

  // ── Read ────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<ClubResponse> listClubs() {
    return clubRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ClubDetailResponse getClubDetails(String clubName) {
    return toDetailResponse(resolveClubByName(clubName));
  }

  // ── Update / Delete ─────────────────────────────────────────────────

  @Transactional
  public ClubResponse updateClub(Long clubId, ClubRequest request) {
    Club club = resolveClub(clubId);
    ensureNameAvailable(request.clubName(), clubId);
    club.setClubName(request.clubName());
    club.setClubFullName(request.clubFullName());
    club.setDescription(request.description());
    return toResponse(club);
  }

  @Transactional
  public void deleteClub(Long clubId) {
    Club club = resolveClub(clubId);
    if (club.isCoordinationClub()) {
      throw new ConflictException("The coordination club cannot be deleted");
    }
    // Children (memberships, join requests, posts, events, profiles...) are
    // removed by DB-level ON DELETE CASCADE constraints.
    clubRepository.delete(club);
  }

  // ── President management ────────────────────────────────────────────

  @Transactional
  public void changePresident(Long clubId, PresidentRequest request) {
    Club club = resolveClub(clubId);
    if (club.isCoordinationClub() && !clubAccessService.isAdmin()) {
      throw new AccessDeniedException("Only ADMIN can change the coordination club president");
    }
    User president = resolveUser(request.presidentUsername());
    ClubMembership target =
        clubMembershipRepository
            .findByUser_UserIdAndClub_ClubId(president.getUserId(), club.getClubId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "User '"
                            + president.getUsername()
                            + "' is not a member of this club"));
    if (target.getClubRole() == ClubRole.CLUB_PRESIDENT) {
      throw new ConflictException("User is already the president of this club");
    }
    clubMembershipRepository
        .findByClub_ClubIdAndClubRole(club.getClubId(), ClubRole.CLUB_PRESIDENT)
        .forEach(current -> current.setClubRole(ClubRole.MEMBER));
    target.setClubRole(ClubRole.CLUB_PRESIDENT);
  }

  // ── Helpers ─────────────────────────────────────────────────────────

  private void assignPresident(Club club, User user) {
    clubMembershipRepository.save(
        ClubMembership.builder()
            .clubRole(ClubRole.CLUB_PRESIDENT)
            .startedAt(new Date())
            .user(user)
            .club(club)
            .build());
  }

  private void ensureNameAvailable(String clubName, Long excludedClubId) {
    clubRepository
        .findByClubName(clubName)
        .map(Club::getClubId)
        .filter(id -> !id.equals(excludedClubId))
        .ifPresent(
            id -> {
              throw new ConflictException("Club name already taken: " + clubName);
            });
  }

  private User resolveUser(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found: " + username));
  }

  private Club resolveClub(Long clubId) {
    return clubRepository
        .findById(clubId)
        .orElseThrow(() -> new NotFoundException("Club not found: " + clubId));
  }

  private Club resolveClubByName(String clubName) {
    return clubRepository
        .findByClubName(clubName)
        .orElseThrow(() -> new NotFoundException("Club not found: " + clubName));
  }

  private ClubResponse toResponse(Club club) {
    return new ClubResponse(
        club.getClubId(),
        club.getClubName(),
        club.getClubFullName(),
        club.getDescription(),
        club.isCoordinationClub(),
        pictureUrl(club.getProfilePicture()),
        clubMembershipRepository.countByClub_ClubId(club.getClubId()));
  }

  private ClubDetailResponse toDetailResponse(Club club) {
    long memberCount = clubMembershipRepository.countByClub_ClubId(club.getClubId());
    List<MemberResponse> staff =
        clubMembershipRepository
            .findByClub_ClubIdAndClubRoleIn(club.getClubId(), STAFF_ROLES)
            .stream()
            .map(this::toMemberResponse)
            .toList();
    return new ClubDetailResponse(
        club.getClubId(),
        club.getClubName(),
        club.getClubFullName(),
        club.getDescription(),
        club.isCoordinationClub(),
        pictureUrl(club.getProfilePicture()),
        memberCount,
        staff);
  }

  private MemberResponse toMemberResponse(ClubMembership membership) {
    User user = membership.getUser();
    ProfilePicture picture = user.getProfilePicture();
    return new MemberResponse(
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        picture == null ? null : s3ObjectStorageService.presignGetUrl(picture.getS3Key()),
        membership.getClubRole());
  }

  private String pictureUrl(ProfilePicture picture) {
    return picture == null ? null : s3ObjectStorageService.presignGetUrl(picture.getS3Key());
  }
}
