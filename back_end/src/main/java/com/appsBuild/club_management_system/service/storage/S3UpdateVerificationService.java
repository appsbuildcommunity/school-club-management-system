package com.appsBuild.club_management_system.service.storage;

import com.appsBuild.club_management_system.repository.AttachmentRepository;
import com.appsBuild.club_management_system.repository.ProfilePictureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
@RequiredArgsConstructor
public class S3UpdateVerificationService {

  private final S3Client s3Client;
  private final AttachmentRepository attachmentRepository;
  private final ProfilePictureRepository profilePictureRepository;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public boolean verifyAndUpdate(String oldKey, String newKey) {
    if (!objectExists(newKey)) {
      return false;
    }

    if (newKey.matches("users/\\d+/profile_picture\\.\\w+")
        || newKey.matches("clubs/\\d+/profile_picture\\.\\w+")) {
      handleProfilePictureUpdate(oldKey, newKey);

    } else if (newKey.matches("clubs/\\d+/events/\\d+/.+\\.\\w+")
        || newKey.matches("clubs/\\d+/posts/\\d+/.+\\.\\w+")) {
      handleAttachmentUpdate(oldKey, newKey);

    } else {
      throw new IllegalArgumentException("Unrecognized object key pattern: " + newKey);
    }

    return true;
  }

  private void handleProfilePictureUpdate(String oldKey, String newKey) {
    profilePictureRepository
        .findByS3Key(oldKey)
        .ifPresentOrElse(
            profilePicture -> {
              profilePicture.setS3Key(newKey);
              profilePictureRepository.save(profilePicture);
              deleteObject(oldKey);
            },
            () -> {
              deleteObject(newKey);
              throw new EntityNotFoundException("Profile picture not found for key: " + oldKey);
            });
  }

  private void handleAttachmentUpdate(String oldKey, String newKey) {
    attachmentRepository
        .findByS3Key(oldKey)
        .ifPresentOrElse(
            attachment -> {
              attachment.setS3Key(newKey);
              attachmentRepository.save(attachment);
              deleteObject(oldKey);
            },
            () -> {
              deleteObject(newKey);
              throw new EntityNotFoundException("Attachment not found for key: " + oldKey);
            });
  }

  private void deleteObject(String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
  }

  private boolean objectExists(String key) {
    try {
      s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }
}
