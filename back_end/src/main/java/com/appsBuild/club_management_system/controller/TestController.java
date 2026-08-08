package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.service.storage.S3PutService;
import com.appsBuild.club_management_system.service.storage.S3UploadVerificationService;

import lombok.AllArgsConstructor;

import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@AllArgsConstructor
public class TestController {

  private final S3PutService s3StorageService;
  private final S3UploadVerificationService vefificationService;

  @GetMapping("/upload-profile-pic")
  @ResponseBody
  public ResponseEntity<S3UploadResponse> getUploadUrl(@RequestParam Long userId, @RequestParam String name) {
    S3UploadResponse presignedUrl = s3StorageService.getUploadUserProfilePicturePresignedUrl(userId, name);
    return ResponseEntity.ok(presignedUrl);
  }
  @PostMapping("/verify")
  @ResponseBody
  public ResponseEntity<Void> verify(@RequestParam String key) throws InterruptedException, ExecutionException {
    vefificationService.verifyAndSave(key); 
    return ResponseEntity.ok().build();
  }
}
