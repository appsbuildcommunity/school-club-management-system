package com.appsBuild.club_management_system.service.storage;

import com.appsBuild.club_management_system.dto.s3Services.response.UploadDtoResponse;
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

  public UploadDtoResponse getUploadUserProfilePicturePresignedUrl(
      Long userId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String key = String.format("users/%d/%s.%s", userId, UUID.randomUUID(), extension);
    return buildResponse(key, originalFilename);
  }

  public UploadDtoResponse getUploadClubProfilePicturePresignedUrl(
      Long clubId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String key = String.format("clubs/%d/%s.%s", clubId, UUID.randomUUID(), extension);
    return buildResponse(key, originalFilename);
  }

  public UploadDtoResponse getUploadEventAttachmentPresignedUrl(
      Long clubId, Long eventId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String key =
        String.format("clubs/%d/events/%d/%s.%s", clubId, eventId, UUID.randomUUID(), extension);
    return buildResponse(key, originalFilename);
  }

  public UploadDtoResponse getUploadPostAttachmentPresignedUrl(
      Long clubId, Long postId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String key =
        String.format("clubs/%d/posts/%d/%s.%s", clubId, postId, UUID.randomUUID(), extension);
    return buildResponse(key, originalFilename);
  }

  private UploadDtoResponse buildResponse(String key, String originalFilename) {
    String contentType = getContentType(originalFilename);
    String uploadUrl = generatePresignedUrl(key, contentType);
    return new UploadDtoResponse(uploadUrl, key);
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
