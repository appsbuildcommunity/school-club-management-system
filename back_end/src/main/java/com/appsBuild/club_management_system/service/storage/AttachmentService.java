package com.appsBuild.club_management_system.service.storage;

import com.appsBuild.club_management_system.dto.s3.S3UpdateResponse;
import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.model.entity.Attachment;
import com.appsBuild.club_management_system.model.entity.Event;
import com.appsBuild.club_management_system.model.entity.Post;
import com.appsBuild.club_management_system.repository.AttachmentRepository;
import com.appsBuild.club_management_system.repository.EventRepository;
import com.appsBuild.club_management_system.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentService {

  private final S3ObjectStorageService storage;
  private final AttachmentRepository attachmentRepository;
  private final EventRepository eventRepository;
  private final PostRepository postRepository;

  public S3UploadResponse requestEventAttachmentUpload(
      Long clubId, Long eventId, String originalFilename) {
    String extension = storage.resolveExtension(originalFilename);
    String key = S3KeyPatterns.eventAttachmentKey(clubId, eventId, extension);
    return buildUploadResponse(key, originalFilename);
  }

  public S3UploadResponse requestPostAttachmentUpload(
      Long clubId, Long postId, String originalFilename) {
    String extension = storage.resolveExtension(originalFilename);
    String key = S3KeyPatterns.postAttachmentKey(clubId, postId, extension);
    return buildUploadResponse(key, originalFilename);
  }

  public S3UpdateResponse updateEventAttachment(
      Long clubId, Long eventId, Long attachmentId, String originalFilename) {
    Attachment current = findAttachment(attachmentId);
    S3UploadResponse upload =
        requestEventAttachmentUpload(clubId, eventId, originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }

  public S3UpdateResponse updatePostAttachment(
      Long clubId, Long postId, Long attachmentId, String originalFilename) {
    Attachment current = findAttachment(attachmentId);
    S3UploadResponse upload =
        requestPostAttachmentUpload(clubId, postId, originalFilename);
    return new S3UpdateResponse(upload.uploadUrl(), current.getS3Key(), upload.key());
  }

  public void deleteAttachment(Long attachmentId) {
    Attachment attachment = findAttachment(attachmentId);
    storage.deleteObject(attachment.getS3Key());
    attachmentRepository.delete(attachment);
  }

  public void handleUpload(String key) {
    if (S3KeyPatterns.isEventAttachment(key)) {
      handleEventAttachment(key);
    } else if (S3KeyPatterns.isPostAttachment(key)) {
      handlePostAttachment(key);
    } else {
      throw new IllegalArgumentException("Unrecognized attachment key: " + key);
    }
  }

  public void handleUpdate(String oldKey, String newKey) {
    if (!S3KeyPatterns.isAttachment(newKey)) {
      throw new IllegalArgumentException("Unrecognized attachment key: " + newKey);
    }
    handleAttachmentUpdate(oldKey, newKey);
  }

  // clubs/{clubId}/events/{eventId}/{uuid}.{ext}
  private void handleEventAttachment(String key) {
    Long eventId = S3KeyPatterns.eventIdFrom(key);
    Optional<Event> eventOpt = eventRepository.findById(eventId);
    if (eventOpt.isEmpty()) {
      // TODO: log warning - event not found for key: key
      storage.deleteObject(key);
      return;
    }
    Attachment attachment = Attachment.builder().s3Key(key).build();
    eventOpt.get().getAttachments().add(attachment);
    eventRepository.save(eventOpt.get());
  }

  // clubs/{clubId}/posts/{postId}/{uuid}.{ext}
  private void handlePostAttachment(String key) {
    Long postId = S3KeyPatterns.postIdFrom(key);
    Optional<Post> postOpt = postRepository.findById(postId);
    if (postOpt.isEmpty()) {
      // TODO: log warning - post not found for key: key
      storage.deleteObject(key);
      return;
    }
    Attachment attachment = Attachment.builder().s3Key(key).build();
    postOpt.get().getAttachments().add(attachment);
    postRepository.save(postOpt.get());
  }

  private void handleAttachmentUpdate(String oldKey, String newKey) {
    attachmentRepository
        .findByS3Key(oldKey)
        .ifPresentOrElse(
            attachment -> {
              attachment.setS3Key(newKey);
              attachmentRepository.save(attachment);
              storage.deleteObject(oldKey);
            },
            () -> {
              storage.deleteObject(newKey);
              throw new EntityNotFoundException(
                  "Attachment not found for key: " + oldKey);
            });
  }

  private S3UploadResponse buildUploadResponse(String key, String originalFilename) {
    String contentType = storage.resolveContentType(originalFilename);
    String uploadUrl = storage.presignPutUrl(key, contentType);
    return new S3UploadResponse(uploadUrl, key);
  }

  private Attachment findAttachment(Long attachmentId) {
    return attachmentRepository
        .findById(attachmentId)
        .orElseThrow(
            () -> new EntityNotFoundException("Attachment not found: " + attachmentId));
  }
}
