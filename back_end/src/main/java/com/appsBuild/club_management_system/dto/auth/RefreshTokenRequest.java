package com.appsBuild.club_management_system.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "refreshToken is required") String refreshToken) {}
