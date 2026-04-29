package com.appsBuild.club_management_system.service.storage;

import com.appsBuild.club_management_system.model.entity.Attachment;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.repository.AttachmentRepository;
import com.appsBuild.club_management_system.repository.ProfilePictureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class S3DeleteService {

  private final S3Client s3Client;
  private final AttachmentRepository attachmentRepository;
  private final ProfilePictureRepository profilePictureRepository;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public void deleteAttachment(Long attachmentId) {
    Attachment attachment =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("Attachment not found: " + attachmentId));

    deleteFromS3(attachment.getS3Key());
    attachmentRepository.delete(attachment);
  }

  public void deleteUserProfilePicture(Long profilePictureId) {
    ProfilePicture profilePicture =
        profilePictureRepository
            .findById(profilePictureId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profile picture not found: " + profilePictureId));

    deleteFromS3(profilePicture.getS3Key());
    profilePictureRepository.delete(profilePicture);
  }

  public void deleteClubProfilePicture(Long profilePictureId) {
    ProfilePicture profilePicture =
        profilePictureRepository
            .findById(profilePictureId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Profile picture not found: " + profilePictureId));

    deleteFromS3(profilePicture.getS3Key());
    profilePictureRepository.delete(profilePicture);
  }

  private void deleteFromS3(String key) {
    DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
    s3Client.deleteObject(request);
  }
}
