package com.appsBuild.club_management_system.service.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3ObjectVerificationService {

  private final S3ObjectStorageService storage;
  private final ProfilePictureService profilePictureService;
  private final AttachmentService attachmentService;

  public boolean verifyAndSave(String objectKey) {
    if (!storage.objectExists(objectKey)) {
      return false;
    }

    if (S3KeyPatterns.isEventAttachment(objectKey)
        || S3KeyPatterns.isPostAttachment(objectKey)) {
      attachmentService.handleUpload(objectKey);

    } else if (S3KeyPatterns.isUserProfilePicture(objectKey)
        || S3KeyPatterns.isClubProfilePicture(objectKey)) {
      profilePictureService.handleUpload(objectKey);

    } else {
      throw new IllegalArgumentException("Unrecognized object key pattern: " + objectKey);
    }

    return true;
  }

  public boolean verifyAndUpdate(String oldKey, String newKey) {
    if (!storage.objectExists(newKey)) {
      return false;
    }

    if (S3KeyPatterns.isEventAttachment(newKey) || S3KeyPatterns.isPostAttachment(newKey)) {
      attachmentService.handleUpdate(oldKey, newKey);

    } else if (S3KeyPatterns.isUserProfilePicture(newKey)
        || S3KeyPatterns.isClubProfilePicture(newKey)) {
      profilePictureService.handleUpdate(oldKey, newKey);

    } else {
      throw new IllegalArgumentException("Unrecognized object key pattern: " + newKey);
    }

    return true;
  }
}
