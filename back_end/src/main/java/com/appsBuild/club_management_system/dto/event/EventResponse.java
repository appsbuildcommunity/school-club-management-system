package com.appsBuild.club_management_system.dto.event;

import java.time.LocalDate;
import java.util.List;

import com.appsBuild.club_management_system.dto.common.AttachmentResponse;
import com.appsBuild.club_management_system.dto.common.ClubReference;
import com.appsBuild.club_management_system.dto.common.CommentResponse;
import com.appsBuild.club_management_system.dto.common.LocationResponse;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;

public record EventResponse(
    Long eventId,
    String title,
    String description,
    ClubReference club,
    VisibilityLevel visibility,
    LocalDate startDate,
    LocalDate endDate,
    List<LocationResponse> locations,
    List<AttachmentResponse> attachments,
    List<CommentResponse> comments,
    List<ReservationResponse> reservations) {}
