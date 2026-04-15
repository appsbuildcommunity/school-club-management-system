package com.appsBuild.club_management_system.webhook.s3.service;

import com.appsBuild.club_management_system.model.entity.Attachment;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.Event;
import com.appsBuild.club_management_system.model.entity.Post;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EventRepository;
import com.appsBuild.club_management_system.repository.PostRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class s3WebhookService {

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  private final UserRepository userRepository;
  private final ClubRepository clubRepository;
  private final EventRepository eventRepository;
  private final PostRepository postRepository;
  private final S3Client s3Client;

  public void processS3PutEvent(String objectKey) {
    if (objectKey == null) {
      return;
    }
    if (objectKey.startsWith("user")) {
      processS3UserPutEvent(objectKey);
    } else if (objectKey.startsWith("club")) {
      processS3ClubPutEvent(objectKey);
    }
  }

  private void processS3UserPutEvent(String objectKey) {
    if (objectKey.contains("profile_picture")) {
      processS3UserProfilePicturePutEvent(objectKey);
    }
  }

  private void processS3UserProfilePicturePutEvent(String objectKey) {
    Pattern pattern = Pattern.compile("users/(\\d+)/profile_picture\\w+");
    Matcher matcher = pattern.matcher(objectKey);
    if (matcher.find()) {
      Long userId = Long.parseLong(matcher.group(1));
      Optional<User> optionalUser = userRepository.findById(userId);
      if (optionalUser.isEmpty()) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        // TODO: the logger should log an appropriate message
      }
      optionalUser.get().setProfilePicture(ProfilePicture.builder().s3Key(objectKey).build());
    } else {
      s3Client.deleteObject(
          DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
      // TODO: the logger should log an appropriate message
    }
  }

  private void processS3ClubPutEvent(String objectKey) {
    if (objectKey.contains("profile_picture")) {
      processS3ClubProfilePicturePutEvent(objectKey);
    } else if (objectKey.contains("/events/")) {
      processS3ClubEventAttachmentPutEvent(objectKey);
    } else if (objectKey.contains("/posts/")) {
      processS3ClubPostAttachmentPutEvent(objectKey);
    }
  }

  private void processS3ClubProfilePicturePutEvent(String objectKey) {
    Pattern pattern = Pattern.compile("clubs/(\\d+)/profile_picture\\w+");
    Matcher matcher = pattern.matcher(objectKey);
    if (matcher.find()) {
      Long clubId = Long.parseLong(matcher.group(1));
      Optional<Club> optionalClub = clubRepository.findById(clubId);
      if (optionalClub.isEmpty()) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        // TODO: log orphaned object deletion
        return;
      }
      optionalClub.get().getProfilePicture().setS3Key(objectKey);
    } else {
      s3Client.deleteObject(
          DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
      // TODO: log malformed key deletion
    }
  }

  private void processS3ClubEventAttachmentPutEvent(String objectKey) {
    // matches: clubs/{clubId}/events/{eventId}/{uuid}.{ext}
    Pattern pattern = Pattern.compile("clubs/(\\d+)/events/(\\d+)/[\\w-]+\\.\\w+");
    Matcher matcher = pattern.matcher(objectKey);
    if (matcher.find()) {
      Long clubId = Long.parseLong(matcher.group(1));
      Long eventId = Long.parseLong(matcher.group(2));
      Optional<Club> optionalClub = clubRepository.findById(clubId);
      if (optionalClub.isEmpty()) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        // TODO: log orphaned object deletion (club not found)
        return;
      }
      Optional<Event> optionalEvent = eventRepository.findById(eventId);
      if (optionalEvent.isEmpty()) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        // TODO: log orphaned object deletion (event not found)
        return;
      }
      optionalEvent.get().getAttachments().add(Attachment.builder().s3Key(objectKey).build());
    } else {
      s3Client.deleteObject(
          DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
      // TODO: log malformed key deletion
    }
  }

  private void processS3ClubPostAttachmentPutEvent(String objectKey) {
    // matches: clubs/{clubId}/posts/{postId}/{uuid}.{ext}
    Pattern pattern = Pattern.compile("clubs/(\\d+)/posts/(\\d+)/[\\w-]+\\.\\w+");
    Matcher matcher = pattern.matcher(objectKey);
    if (matcher.find()) {
      Long clubId = Long.parseLong(matcher.group(1));
      Long postId = Long.parseLong(matcher.group(2));
      Optional<Club> optionalClub = clubRepository.findById(clubId);
      if (optionalClub.isEmpty()) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        // TODO: log orphaned object deletion (club not found)
        return;
      }
      Optional<Post> optionalPost = postRepository.findById(postId);
      if (optionalPost.isEmpty()) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        // TODO: log orphaned object deletion (post not found)
        return;
      }
      optionalPost.get().getAttachments().add(Attachment.builder().s3Key(objectKey).build());
    } else {
      s3Client.deleteObject(
          DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
      // TODO: log malformed key deletion
    }
  }
}
