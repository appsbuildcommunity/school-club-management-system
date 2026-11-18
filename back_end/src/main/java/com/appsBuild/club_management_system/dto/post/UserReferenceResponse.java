package com.appsBuild.club_management_system.dto.post;

public record UserReferenceResponse(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    ProfilePictureRef profilePicture
) {
  public record ProfilePictureRef(String url) {}
}
