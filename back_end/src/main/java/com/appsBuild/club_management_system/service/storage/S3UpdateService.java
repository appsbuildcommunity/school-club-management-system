package com.appsBuild.club_management_system.service.storage;

import com.appsBuild.club_management_system.dto.s3.S3UpdateResponse;
import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.model.entity.Attachment;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.repository.AttachmentRepository;
import com.appsBuild.club_management_system.repository.ProfilePictureRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class S3UpdateService {

  private final S3PutService s3PutService;
  private final AttachmentRepository attachmentRepository;
  private final ProfilePictureRepository profilePictureRepository;

  public S3UpdateService(
      S3PutService s3PutService,
      AttachmentRepository attachmentRepository,
      ProfilePictureRepository profilePictureRepository) {
    this.s3PutService = s3PutService;
    this.attachmentRepository = attachmentRepository;
    this.profilePictureRepository = profilePictureRepository;
  }

  public S3UpdateResponse updateUserProfilePicture(
      Long profilePictureId, String originalFilename) {
    ProfilePicture current =
        profilePictureRepository
            .findById(profilePictureId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profile picture not found: " + profilePictureId));

    S3UploadResponse upload =
        s3PutService.getUploadUserProfilePicturePresignedUrl(
            current.getUser().getUserId(), originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }

  public S3UpdateResponse updateClubProfilePicture(
      Long profilePictureId, String originalFilename) {
    ProfilePicture current =
        profilePictureRepository
            .findById(profilePictureId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profile picture not found: " + profilePictureId));

    S3UploadResponse upload =
        s3PutService.getUploadClubProfilePicturePresignedUrl(
            current.getClub().getClubId(), originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }

  public S3UpdateResponse updateEventAttachment(
      Long clubId, Long eventId, Long attachmentId, String originalFilename) {
    Attachment current =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("Attachment not found: " + attachmentId));

    S3UploadResponse upload =
        s3PutService.getUploadEventAttachmentPresignedUrl(clubId, eventId, originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }

  public S3UpdateResponse updatePostAttachment(
      Long clubId, Long postId, Long attachmentId, String originalFilename) {
    Attachment current =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("Attachment not found: " + attachmentId));

    S3UploadResponse upload =
        s3PutService.getUploadPostAttachmentPresignedUrl(clubId, postId, originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }
}
