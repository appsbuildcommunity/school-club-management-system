package com.appsBuild.club_management_system.dto.club;

public record ClubResponse(
    Long clubId,
    String clubName,
    String clubFullName,
    String description,
    boolean isCoordinationClub,
    String profilePictureUrl,
    long memberCount) {}
