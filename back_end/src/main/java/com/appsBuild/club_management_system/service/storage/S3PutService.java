package com.appsBuild.club_management_system.service.storage;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3PutService {

  private final S3Presigner s3Presigner;

  public S3PutService(S3Presigner s3Presigner) {
    this.s3Presigner = s3Presigner;
  }

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(5);

  /**
   * Generate a presigned URL for uploading a user's profile picture.
   *
   * @param userId The ID of the user
   * @param originalFilename The name of the file
   * @return Presigned URL (PUT method)
   */
  public String getUploadUserProfilePicturePresignedUrl(Long userId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String key = String.format("users/%d/profile_picture.%s", userId, extension);
    String contentType = getContentType(originalFilename);
    return generatePresignedUrl(key, contentType);
  }

  /**
   * Generate a presigned URL for uploading a club's profile picture.
   *
   * @param clubId The ID of the club
   * @param originalFilename The name of the file
   * @return Presigned URL
   */
  public String getUploadClubProfilePicturePresignedUrl(Long clubId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String key = String.format("clubs/%d/profile_picture.%s", clubId, extension);
    String contentType = getContentType(originalFilename);
    return generatePresignedUrl(key, contentType);
  }

  /**
   * Generate a presigned URL for uploading an event attachment (PDF, DOC, etc.).
   *
   * @param clubId Club ID
   * @param eventId Event ID
   * @param originalFilename Original filename
   * @return Presigned URL
   */
  public String getUploadEventAttachmentPresignedUrl(
      Long clubId, Long eventId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String uniqueId = UUID.randomUUID().toString();
    String key = String.format("clubs/%d/events/%d/%s.%s", clubId, eventId, uniqueId, extension);
    String contentType = getContentType(originalFilename);
    return generatePresignedUrl(key, contentType);
  }

  /**
   * Generate a presigned URL for uploading a post attachment.
   *
   * @param clubId Club ID
   * @param postId Post ID
   * @param originalFilename Original filename
   * @return Presigned URL
   */
  public String getUploadPostAttachmentPresignedUrl(
      Long clubId, Long postId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String uniqueId = UUID.randomUUID().toString();
    String key = String.format("clubs/%d/posts/%d/%s.%s", clubId, postId, uniqueId, extension);
    String contentType = getContentType(originalFilename);
    return generatePresignedUrl(key, contentType);
  }

  private String generatePresignedUrl(String key, String contentType) {
    PutObjectRequest objectRequest =
        PutObjectRequest.builder().bucket(bucketName).key(key).contentType(contentType).build();

    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(PRESIGNED_URL_EXPIRATION)
            .putObjectRequest(objectRequest)
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
    return presignedRequest.url().toString();
  }

  private String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "bin";
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }

  private String getContentType(String fileName) {
    return MediaTypeFactory.getMediaType(fileName)
        .map(MediaType::toString)
        .orElse("application/octet-stream");
  }
}
