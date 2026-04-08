package com.appsBuild.club_management_system.service.storage;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class S3GetService {

  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(15);

  public S3GetService(S3Presigner s3Presigner) {
    this.s3Presigner = s3Presigner;
  }

  /**
   * Generate a presigned GET URL for any S3 object using its stored key.
   *
   * @param s3Key The full S3 object key
   * @return Presigned URL (GET method)
   */
  public String getPresignedUrl(String s3Key) {
    GetObjectRequest objectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(s3Key).build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(PRESIGNED_URL_EXPIRATION)
            .getObjectRequest(objectRequest)
            .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
    return presignedRequest.url().toString();
  }
}
