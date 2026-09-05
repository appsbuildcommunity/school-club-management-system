package com.appsBuild.club_management_system.dto.club;

import com.appsBuild.club_management_system.model.enums.ClubRole;

public record MemberResponse(
    String username, String firstName, String lastName, String profilePictureUrl, ClubRole role) {}
