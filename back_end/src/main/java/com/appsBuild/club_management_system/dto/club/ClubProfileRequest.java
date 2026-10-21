package com.appsBuild.club_management_system.dto.club;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ClubProfileRequest(
    @NotBlank(message = "name is required") String name,
    List<String> endpoints) {}
