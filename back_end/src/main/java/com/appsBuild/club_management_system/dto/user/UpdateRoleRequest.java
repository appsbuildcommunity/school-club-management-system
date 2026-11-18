package com.appsBuild.club_management_system.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(@NotBlank(message = "role is required") String role) {}
