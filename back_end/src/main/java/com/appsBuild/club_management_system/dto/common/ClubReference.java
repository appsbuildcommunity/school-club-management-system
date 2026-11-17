package com.appsBuild.club_management_system.dto.common;

public record ClubReference(
    Long id, String name, String description, ImageReference profilePicture) {}
