package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.dto.user.UpdateRoleRequest;
import com.appsBuild.club_management_system.dto.user.UserMeResponse;
import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.UserRepository;
import com.appsBuild.club_management_system.service.keycloak.KeycloakAdminService;
import com.appsBuild.club_management_system.service.storage.S3GetService;
import com.appsBuild.club_management_system.service.storage.S3PutService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private static final Set<String> ASSIGNABLE_ROLES = Set.of("USER", "STUDENT");

  private final UserRepository userRepository;
  private final S3GetService s3GetService;
  private final S3PutService s3PutService;
  private final KeycloakAdminService keycloakAdminService;

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
            profilePictureUrl,
            rolesFromJwt(jwt)));
  }

  @GetMapping("/me/profile-picture/upload-url")
  public ResponseEntity<S3UploadResponse> profilePictureUploadUrl(
      @RequestParam String name, @AuthenticationPrincipal Jwt jwt) {
    User user = currentUser(jwt);
    S3UploadResponse response =
        s3PutService.getUploadUserProfilePicturePresignedUrl(user.getUserId(), name);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{userId}/role")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> updateRole(
      @PathVariable Long userId, @RequestBody UpdateRoleRequest request) {
    String targetRole = normalizeRole(request.role());
    if (targetRole == null) {
      throw new ApplicationException("role must be one of: USER, STUDENT");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

    keycloakAdminService.updateRealmRole(user.getKeycloakSub(), targetRole);
    return ResponseEntity.ok().build();
  }

  private String normalizeRole(String role) {
    if (role == null) {
      return null;
    }
    String normalized = role.trim().toUpperCase();
    return ASSIGNABLE_ROLES.contains(normalized) ? normalized : null;
  }

  private List<String> rolesFromJwt(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
    if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
      return List.of();
    }
    return roles.stream().map(String::valueOf).toList();
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
