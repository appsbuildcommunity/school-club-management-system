package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.s3Services.response.UploadDtoResponse;
import com.appsBuild.club_management_system.dto.user.UserMeResponse;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.UserRepository;
import com.appsBuild.club_management_system.service.storage.S3GetService;
import com.appsBuild.club_management_system.service.storage.S3PutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final S3GetService s3GetService;
  private final S3PutService s3PutService;

  @GetMapping("/me")
  public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal Jwt jwt) {
    User user = currentUser(jwt);

    String profilePictureUrl = null;
    if (user.getProfilePicture() != null && user.getProfilePicture().getS3Key() != null) {
      profilePictureUrl = s3GetService.getPresignedUrl(user.getProfilePicture().getS3Key());
    }

    return ResponseEntity.ok(
        new UserMeResponse(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            profilePictureUrl));
  }

  @GetMapping("/me/profile-picture/upload-url")
  public ResponseEntity<UploadDtoResponse> profilePictureUploadUrl(
      @RequestParam String name, @AuthenticationPrincipal Jwt jwt) {
    User user = currentUser(jwt);
    UploadDtoResponse response =
        s3PutService.getUploadUserProfilePicturePresignedUrl(user.getUserId(), name);
    return ResponseEntity.ok(response);
  }

  private User currentUser(Jwt jwt) {
    return userRepository
        .findByKeycloakSub(jwt.getSubject())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "User not found for keycloak subject: " + jwt.getSubject()));
  }
}
