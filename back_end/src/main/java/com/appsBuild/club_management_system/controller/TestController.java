package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.s3.S3UpdateResponse;
import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.service.storage.AttachmentService;
import com.appsBuild.club_management_system.service.storage.ProfilePictureService;
import com.appsBuild.club_management_system.service.storage.S3ObjectStorageService;
import com.appsBuild.club_management_system.service.storage.S3ObjectVerificationService;

import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@AllArgsConstructor
public class TestController {

  private final ProfilePictureService profilePictureService;
  private final AttachmentService attachmentService;
  private final S3ObjectVerificationService verificationService;
  private final S3ObjectStorageService storageService;

  @GetMapping("/upload-profile-pic")
  @ResponseBody
  public ResponseEntity<S3UploadResponse> uploadUserProfilePic(
      @RequestParam Long userId, @RequestParam String name) {
    return ResponseEntity.ok(profilePictureService.requestUserProfilePictureUpload(userId, name));
  }

  @GetMapping("/upload-club-profile-pic")
  @ResponseBody
  public ResponseEntity<S3UploadResponse> uploadClubProfilePic(
      @RequestParam Long clubId, @RequestParam String name) {
    return ResponseEntity.ok(profilePictureService.requestClubProfilePictureUpload(clubId, name));
  }

  @GetMapping("/update-profile-pic")
  @ResponseBody
  public ResponseEntity<S3UpdateResponse> updateUserProfilePic(
      @RequestParam Long profilePictureId, @RequestParam String name) {
    return ResponseEntity.ok(
        profilePictureService.updateUserProfilePicture(profilePictureId, name));
  }

  @GetMapping("/update-club-profile-pic")
  @ResponseBody
  public ResponseEntity<S3UpdateResponse> updateClubProfilePic(
      @RequestParam Long profilePictureId, @RequestParam String name) {
    return ResponseEntity.ok(
        profilePictureService.updateClubProfilePicture(profilePictureId, name));
  }

  @DeleteMapping("/delete-user-profile-pic")
  @ResponseBody
  public ResponseEntity<Void> deleteUserProfilePic(@RequestParam Long profilePictureId) {
    profilePictureService.deleteUserProfilePicture(profilePictureId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/delete-club-profile-pic")
  @ResponseBody
  public ResponseEntity<Void> deleteClubProfilePic(@RequestParam Long profilePictureId) {
    profilePictureService.deleteClubProfilePicture(profilePictureId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/upload-event-attachment")
  @ResponseBody
  public ResponseEntity<S3UploadResponse> uploadEventAttachment(
      @RequestParam Long clubId, @RequestParam Long eventId, @RequestParam String name) {
    return ResponseEntity.ok(
        attachmentService.requestEventAttachmentUpload(clubId, eventId, name));
  }

  @GetMapping("/upload-post-attachment")
  @ResponseBody
  public ResponseEntity<S3UploadResponse> uploadPostAttachment(
      @RequestParam Long clubId, @RequestParam Long postId, @RequestParam String name) {
    return ResponseEntity.ok(
        attachmentService.requestPostAttachmentUpload(clubId, postId, name));
  }

  @GetMapping("/update-event-attachment")
  @ResponseBody
  public ResponseEntity<S3UpdateResponse> updateEventAttachment(
      @RequestParam Long clubId,
      @RequestParam Long eventId,
      @RequestParam Long attachmentId,
      @RequestParam String name) {
    return ResponseEntity.ok(
        attachmentService.updateEventAttachment(clubId, eventId, attachmentId, name));
  }

  @GetMapping("/update-post-attachment")
  @ResponseBody
  public ResponseEntity<S3UpdateResponse> updatePostAttachment(
      @RequestParam Long clubId,
      @RequestParam Long postId,
      @RequestParam Long attachmentId,
      @RequestParam String name) {
    return ResponseEntity.ok(
        attachmentService.updatePostAttachment(clubId, postId, attachmentId, name));
  }

  @DeleteMapping("/delete-attachment")
  @ResponseBody
  public ResponseEntity<Void> deleteAttachment(@RequestParam Long attachmentId) {
    attachmentService.deleteAttachment(attachmentId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/verify")
  @ResponseBody
  public ResponseEntity<Map<String, Boolean>> verify(@RequestParam String key)
      throws InterruptedException, ExecutionException {
    boolean verified = verificationService.verifyAndSave(key);
    return ResponseEntity.ok(Map.of("verified", verified));
  }

  @PostMapping("/verify-update")
  @ResponseBody
  public ResponseEntity<Map<String, Boolean>> verifyUpdate(
      @RequestParam String oldKey, @RequestParam String newKey)
      throws InterruptedException, ExecutionException {
    boolean verified = verificationService.verifyAndUpdate(oldKey, newKey);
    return ResponseEntity.ok(Map.of("verified", verified));
  }

  @GetMapping("/download")
  @ResponseBody
  public ResponseEntity<Map<String, String>> download(@RequestParam String key) {
    return ResponseEntity.ok(Map.of("url", storageService.presignGetUrl(key)));
  }

  @GetMapping("/object-exists")
  @ResponseBody
  public ResponseEntity<Map<String, Boolean>> objectExists(@RequestParam String key) {
    return ResponseEntity.ok(Map.of("exists", storageService.objectExists(key)));
  }
}
