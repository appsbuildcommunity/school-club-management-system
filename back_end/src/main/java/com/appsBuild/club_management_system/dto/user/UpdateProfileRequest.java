package com.appsBuild.club_management_system.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank(message = "username is required")
        @Size(max = 50, message = "username must be at most 50 characters")
        @Pattern(
            regexp = "[A-Za-z0-9._-]+",
            message = "username may only contain letters, numbers, '.', '_' and '-'")
        String username,
    @NotBlank(message = "firstName is required")
        @Size(max = 50, message = "firstName must be at most 50 characters")
        String firstName,
    @Size(max = 50, message = "lastName must be at most 50 characters") String lastName) {}
