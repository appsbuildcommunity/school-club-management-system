package com.appsBuild.club_management_system.service.storage;

import com.appsBuild.club_management_system.dto.s3Services.response.UpdateDtoResponse;
import com.appsBuild.club_management_system.dto.s3Services.response.UploadDtoResponse;
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

  public UpdateDtoResponse updateUserProfilePicture(
      Long profilePictureId, String originalFilename) {
    ProfilePicture current =
        profilePictureRepository
            .findById(profilePictureId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profile picture not found: " + profilePictureId));

    UploadDtoResponse upload =
        s3PutService.getUploadUserProfilePicturePresignedUrl(
            current.getUser().getUserId(), originalFilename);
    return new UpdateDtoResponse(upload.getUploadUrl(), current.getS3Key(), upload.getKey());
  }

  public UpdateDtoResponse updateClubProfilePicture(
      Long profilePictureId, String originalFilename) {
    ProfilePicture current =
        profilePictureRepository
            .findById(profilePictureId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profile picture not found: " + profilePictureId));

    UploadDtoResponse upload =
        s3PutService.getUploadClubProfilePicturePresignedUrl(
            current.getClub().getClubId(), originalFilename);
    return new UpdateDtoResponse(upload.getUploadUrl(), current.getS3Key(), upload.getKey());
  }

  public UpdateDtoResponse updateEventAttachment(
      Long clubId, Long eventId, Long attachmentId, String originalFilename) {
    Attachment current =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("Attachment not found: " + attachmentId));

    UploadDtoResponse upload =
        s3PutService.getUploadEventAttachmentPresignedUrl(clubId, eventId, originalFilename);
    return new UpdateDtoResponse(upload.getUploadUrl(), current.getS3Key(), upload.getKey());
  }

  public UpdateDtoResponse updatePostAttachment(
      Long clubId, Long postId, Long attachmentId, String originalFilename) {
    Attachment current =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("Attachment not found: " + attachmentId));

    UploadDtoResponse upload =
        s3PutService.getUploadPostAttachmentPresignedUrl(clubId, postId, originalFilename);
    return new UpdateDtoResponse(upload.getUploadUrl(), current.getS3Key(), upload.getKey());
  }
}
