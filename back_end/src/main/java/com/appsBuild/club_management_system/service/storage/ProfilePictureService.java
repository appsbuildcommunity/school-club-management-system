package com.appsBuild.club_management_system.service.storage;

import com.appsBuild.club_management_system.dto.s3.S3UpdateResponse;
import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.ProfilePictureRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfilePictureService {

  private final S3ObjectStorageService storage;
  private final ProfilePictureRepository profilePictureRepository;
  private final UserRepository userRepository;
  private final ClubRepository clubRepository;

  public S3UploadResponse requestUserProfilePictureUpload(Long userId, String originalFilename) {
    String extension = storage.resolveExtension(originalFilename);
    String key = S3KeyPatterns.userProfilePictureKey(userId, extension);
    return buildUploadResponse(key, originalFilename);
  }

  public S3UploadResponse requestClubProfilePictureUpload(Long clubId, String originalFilename) {
    String extension = storage.resolveExtension(originalFilename);
    String key = S3KeyPatterns.clubProfilePictureKey(clubId, extension);
    return buildUploadResponse(key, originalFilename);
  }

  public S3UpdateResponse updateUserProfilePicture(
      Long profilePictureId, String originalFilename) {
    ProfilePicture current = findProfilePicture(profilePictureId);
    S3UploadResponse upload =
        requestUserProfilePictureUpload(current.getUser().getUserId(), originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }

  public S3UpdateResponse updateClubProfilePicture(
      Long profilePictureId, String originalFilename) {
    ProfilePicture current = findProfilePicture(profilePictureId);
    S3UploadResponse upload =
        requestClubProfilePictureUpload(current.getClub().getClubId(), originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }

  public void deleteUserProfilePicture(Long profilePictureId) {
    ProfilePicture profilePicture = findProfilePicture(profilePictureId);
    storage.deleteObject(profilePicture.getS3Key());
    profilePictureRepository.delete(profilePicture);
  }

  public void deleteClubProfilePicture(Long profilePictureId) {
    ProfilePicture profilePicture = findProfilePicture(profilePictureId);
    storage.deleteObject(profilePicture.getS3Key());
    profilePictureRepository.delete(profilePicture);
  }

  public void handleUpload(String key) {
    if (S3KeyPatterns.isUserProfilePicture(key)) {
      handleUserProfilePicture(key);
    } else if (S3KeyPatterns.isClubProfilePicture(key)) {
      handleClubProfilePicture(key);
    } else {
      throw new IllegalArgumentException("Unrecognized profile picture key: " + key);
    }
  }

  public void handleUpdate(String oldKey, String newKey) {
    if (!S3KeyPatterns.isProfilePicture(newKey)) {
      throw new IllegalArgumentException("Unrecognized profile picture key: " + newKey);
    }
    handleProfilePictureUpdate(oldKey, newKey);
  }

  // users/{userId}/{uuid}.{ext}
  private void handleUserProfilePicture(String key) {
    Long userId = S3KeyPatterns.userIdFrom(key);
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      // TODO: log warning - user not found for key: key
      storage.deleteObject(key);
      return;
    }
    ProfilePicture profilePicture = ProfilePicture.builder().s3Key(key).build();
    userOpt.get().setProfilePicture(profilePicture);
    userRepository.save(userOpt.get());
  }

  // clubs/{clubId}/{uuid}.{ext}
  private void handleClubProfilePicture(String key) {
    Long clubId = S3KeyPatterns.clubIdFrom(key);
    Optional<Club> clubOpt = clubRepository.findById(clubId);
    if (clubOpt.isEmpty()) {
      // TODO: log warning - club not found for key: key
      storage.deleteObject(key);
      return;
    }
    ProfilePicture profilePicture = ProfilePicture.builder().s3Key(key).build();
    clubOpt.get().setProfilePicture(profilePicture);
    clubRepository.save(clubOpt.get());
  }

  private void handleProfilePictureUpdate(String oldKey, String newKey) {
    profilePictureRepository
        .findByS3Key(oldKey)
        .ifPresentOrElse(
            profilePicture -> {
              profilePicture.setS3Key(newKey);
              profilePictureRepository.save(profilePicture);
              storage.deleteObject(oldKey);
            },
            () -> {
              storage.deleteObject(newKey);
              throw new EntityNotFoundException(
                  "Profile picture not found for key: " + oldKey);
            });
  }

  private S3UploadResponse buildUploadResponse(String key, String originalFilename) {
    String contentType = storage.resolveContentType(originalFilename);
    String uploadUrl = storage.presignPutUrl(key, contentType);
    return new S3UploadResponse(uploadUrl, key);
  }

  private ProfilePicture findProfilePicture(Long profilePictureId) {
    return profilePictureRepository
        .findById(profilePictureId)
        .orElseThrow(
            () -> new EntityNotFoundException("Profile picture not found: " + profilePictureId));
  }
}
