package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.service.storage.S3StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@AllArgsConstructor
public class TestController {

  private final S3StorageService s3StorageService;

  @GetMapping("/upload-profile-pic")
  @ResponseBody
  public ResponseEntity<String> getUploadUrl(@RequestParam Long userId, @RequestParam String name) {
    String presignedUrl = s3StorageService.getUploadUserProfilePicturePresignedUrl(userId, name);
    return ResponseEntity.ok(presignedUrl);
  }
}
