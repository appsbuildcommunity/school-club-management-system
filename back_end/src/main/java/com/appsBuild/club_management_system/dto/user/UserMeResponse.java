package com.appsBuild.club_management_system.dto.user;

public record UserMeResponse(
    Long userId,
    String username,
    String email,
    String firstName,
    String lastName,
    String profilePictureUrl) {}
