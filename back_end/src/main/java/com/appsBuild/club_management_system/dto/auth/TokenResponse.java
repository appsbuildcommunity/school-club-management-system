package com.appsBuild.club_management_system.dto.auth;

public record TokenResponse(
    String accessToken, String refreshToken, String tokenType, long expiresIn) {}
