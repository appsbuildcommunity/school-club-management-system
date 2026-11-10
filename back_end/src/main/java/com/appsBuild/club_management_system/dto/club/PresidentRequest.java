package com.appsBuild.club_management_system.dto.club;

import jakarta.validation.constraints.NotBlank;

public record PresidentRequest(
    @NotBlank(message = "presidentUsername is required") String presidentUsername) {}
