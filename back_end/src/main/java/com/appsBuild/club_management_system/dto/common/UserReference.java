package com.appsBuild.club_management_system.dto.common;

public record UserReference(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    ImageReference profilePicture) {}
