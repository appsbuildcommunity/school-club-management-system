package com.appsBuild.club_management_system.dto.post;

public record ClubReferenceResponse(
    Long id,
    String name,
    String description,
    ProfilePictureRef profilePicture
) {
  public record ProfilePictureRef(String url) {}
}
