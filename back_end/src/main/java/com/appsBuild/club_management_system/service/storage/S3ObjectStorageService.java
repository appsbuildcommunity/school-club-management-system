package com.appsBuild.club_management_system.service.storage;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3ObjectStorageService {

  private static final Duration PUT_URL_EXPIRATION = Duration.ofMinutes(5);
  private static final Duration GET_URL_EXPIRATION = Duration.ofMinutes(15);

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public S3ObjectStorageService(S3Presigner s3Presigner, S3Client s3Client) {
    this.s3Presigner = s3Presigner;
    this.s3Client = s3Client;
  }

  public String presignPutUrl(String key, String contentType) {
    PutObjectRequest objectRequest =
        PutObjectRequest.builder().bucket(bucketName).key(key).contentType(contentType).build();

    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(PUT_URL_EXPIRATION)
            .putObjectRequest(objectRequest)
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
    return presignedRequest.url().toString();
  }

  public String presignGetUrl(String s3Key) {
    GetObjectRequest objectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(s3Key).build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(GET_URL_EXPIRATION)
            .getObjectRequest(objectRequest)
            .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
    return presignedRequest.url().toString();
  }

  public void deleteObject(String key) {
    DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
    s3Client.deleteObject(request);
  }

  public boolean objectExists(String key) {
    try {
      s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  public String resolveContentType(String fileName) {
    return MediaTypeFactory.getMediaType(fileName)
        .map(MediaType::toString)
        .orElse("application/octet-stream");
  }

  public String resolveExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "bin";
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }
}
