package com.appsBuild.club_management_system.dto.event;

import java.util.Date;

import com.appsBuild.club_management_system.dto.common.LocationResponse;
import com.appsBuild.club_management_system.model.enums.DemandStatus;

public record ReservationResponse(
    Long id,
    Date startDate,
    Date endDate,
    DemandStatus status,
    LocationResponse location) {}
