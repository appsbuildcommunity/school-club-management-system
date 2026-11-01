package com.appsBuild.club_management_system.dto.user;

import java.util.List;

public record UserResponse(
    Long userId,
    String username,
    String email,
    String firstName,
    String lastName,
    String profilePictureUrl,
    List<String> roles) {}
