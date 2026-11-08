package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.annotation.Grantable;
import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.dto.user.UpdateProfileRequest;
import com.appsBuild.club_management_system.dto.user.UpdateRoleRequest;
import com.appsBuild.club_management_system.dto.user.UserClubsResponse;
import com.appsBuild.club_management_system.dto.user.UserProfileResponse;
import com.appsBuild.club_management_system.dto.user.UserResponse;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser() {
    return ResponseEntity.ok(userService.getCurrentUser());
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.getUserProfile(userId));
  }

  @PatchMapping("/me")
  public ResponseEntity<UserResponse> updateCurrentUser(
      @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
  }

  @PatchMapping("/{userId}/role")
  @Grantable(
      name = "change_user_role",
      description = "Change a user's role between USER and STUDENT",
      category = Category.MANAGE_USERS,
      privileged = true)
  @PreAuthorize(
      "hasRole('ADMIN') or @clubAccess.hasCoordinationEndpoint('change_user_role')")
  public ResponseEntity<Void> updateRole(
      @PathVariable Long userId, @Valid @RequestBody UpdateRoleRequest request) {
    userService.updateRole(userId, request.role());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/me/profile-picture/upload-url")
  public ResponseEntity<S3UploadResponse> getProfilePictureUploadUrl(
      @RequestParam("name") String name) {
    return ResponseEntity.ok(userService.requestProfilePictureUpload(name));
  }

  @GetMapping("/me/clubs")
  public ResponseEntity<UserClubsResponse> getCurrentUserClubs() {
    return ResponseEntity.ok(userService.getCurrentUserClubs());
  }
}
