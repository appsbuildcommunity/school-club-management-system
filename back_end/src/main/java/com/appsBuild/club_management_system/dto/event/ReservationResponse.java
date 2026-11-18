package com.appsBuild.club_management_system.dto.event;

import java.time.LocalDate;

import com.appsBuild.club_management_system.dto.common.LocationResponse;
import com.appsBuild.club_management_system.model.enums.DemandStatus;

public record ReservationResponse(
    Long id,
    LocalDate startDate,
    LocalDate endDate,
    DemandStatus status,
    LocationResponse location) {}
