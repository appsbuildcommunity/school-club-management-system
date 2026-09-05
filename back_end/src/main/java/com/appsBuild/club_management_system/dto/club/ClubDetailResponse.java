package com.appsBuild.club_management_system.dto.club;

import java.util.List;

public record ClubDetailResponse(
    Long clubId,
    String clubName,
    String clubFullName,
    String description,
    boolean isCoordinationClub,
    String profilePictureUrl,
    long memberCount,
    List<MemberResponse> staff) {}
