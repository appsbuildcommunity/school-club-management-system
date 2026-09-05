package com.appsBuild.club_management_system.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClubRequest(
    @NotBlank(message = "clubName is required") @Size(max = 100) String clubName,
    @Size(max = 200) String clubFullName,
    @Size(max = 1000) String description,
    String presidentUsername) {}
