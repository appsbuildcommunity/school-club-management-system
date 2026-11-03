package com.appsBuild.club_management_system.dto.user;

import com.appsBuild.club_management_system.model.enums.ClubRole;
import java.util.Date;

public record UserClubResponse(
    Long membershipId,
    String clubName,
    String clubFullName,
    String description,
    boolean isCoordinationClub,
    String profilePictureUrl,
    ClubRole clubRole,
    Date joinedDate) {}
