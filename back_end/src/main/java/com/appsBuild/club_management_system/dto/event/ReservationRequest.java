package com.appsBuild.club_management_system.dto.event;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
    @NotNull Long locationId, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
