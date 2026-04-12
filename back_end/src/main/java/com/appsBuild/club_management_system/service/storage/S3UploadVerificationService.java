package com.appsBuild.club_management_system.service.storage;

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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
@RequiredArgsConstructor
public class S3UploadVerificationService {

  private final S3Client s3Client;
  private final UserRepository userRepository;
  private final ClubRepository clubRepository;
  private final EventRepository eventRepository;
  private final PostRepository postRepository;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public boolean verifyAndSave(String objectKey) {
    if (!objectExists(objectKey)) {
      return false;
    }

    // users/{userId}/profile_picture.{ext}
    if (objectKey.matches("users/\\d+/profile_picture\\.\\w+")) {
      handleUserProfilePicture(objectKey);

      // clubs/{clubId}/profile_picture.{ext}
    } else if (objectKey.matches("clubs/\\d+/profile_picture\\.\\w+")) {
      handleClubProfilePicture(objectKey);

      // clubs/{clubId}/events/{eventId}/{uuid}.{ext}
    } else if (objectKey.matches("clubs/\\d+/events/\\d+/.+\\.\\w+")) {
      handleEventAttachment(objectKey);

      // clubs/{clubId}/posts/{postId}/{uuid}.{ext}
    } else if (objectKey.matches("clubs/\\d+/posts/\\d+/.+\\.\\w+")) {
      handlePostAttachment(objectKey);

    } else {
      throw new IllegalArgumentException("Unrecognized object key pattern: " + objectKey);
    }

    return true;
  }

  // users/{userId}/profile_picture.{ext}
  private void handleUserProfilePicture(String key) {
    String[] parts = key.split("/");
    Long userId = Long.parseLong(parts[1]);
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      // TODO: log warning - user not found for key: key
      deleteObject(key);
      return;
    }
    ProfilePicture profilePicture = ProfilePicture.builder().s3Key(key).build();
    userOpt.get().setProfilePicture(profilePicture);
    userRepository.save(userOpt.get());
  }

  // clubs/{clubId}/profile_picture.{ext}
  private void handleClubProfilePicture(String key) {
    String[] parts = key.split("/");
    Long clubId = Long.parseLong(parts[1]);
    Optional<Club> clubOpt = clubRepository.findById(clubId);
    if (clubOpt.isEmpty()) {
      // TODO: log warning - club not found for key: key
      deleteObject(key);
      return;
    }
    ProfilePicture profilePicture = ProfilePicture.builder().s3Key(key).build();
    clubOpt.get().setProfilePicture(profilePicture);
    clubRepository.save(clubOpt.get());
  }

  // clubs/{clubId}/events/{eventId}/{uuid}.{ext}
  private void handleEventAttachment(String key) {
    String[] parts = key.split("/");
    Long eventId = Long.parseLong(parts[3]);
    Optional<Event> eventOpt = eventRepository.findById(eventId);
    if (eventOpt.isEmpty()) {
      // TODO: log warning - event not found for key: key
      deleteObject(key);
      return;
    }
    Attachment attachment = Attachment.builder().s3Key(key).build();
    eventOpt.get().getAttachments().add(attachment);
    eventRepository.save(eventOpt.get());
  }

  // clubs/{clubId}/posts/{postId}/{uuid}.{ext}
  private void handlePostAttachment(String key) {
    String[] parts = key.split("/");
    Long postId = Long.parseLong(parts[3]);
    Optional<Post> postOpt = postRepository.findById(postId);
    if (postOpt.isEmpty()) {
      // TODO: log warning - post not found for key: key
      deleteObject(key);
      return;
    }
    Attachment attachment = Attachment.builder().s3Key(key).build();
    postOpt.get().getAttachments().add(attachment);
    postRepository.save(postOpt.get());
  }

  private void deleteObject(String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
  }

  private boolean objectExists(String objectKey) {
    try {
      s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }
}
