package com.appsBuild.club_management_system.dto.event;

import java.time.LocalDate;
import java.util.List;

import com.appsBuild.club_management_system.model.enums.VisibilityLevel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRequest {

	@NotBlank
	private String title;

	private String description;

	@NotNull
	private VisibilityLevel visibility;

	@NotNull
	private LocalDate startDate;

	@NotNull
	private LocalDate endDate;

	private List<@Valid ReservationRequest> reservations;
}
