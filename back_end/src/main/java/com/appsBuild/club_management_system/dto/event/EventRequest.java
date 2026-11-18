package com.appsBuild.club_management_system.dto.event;

import java.time.LocalDate;
import java.util.List;

import com.appsBuild.club_management_system.model.enums.VisibilityLevel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EventRequest {

	@NotBlank
	@Size(max = 200)
	private String title;

	@Size(max = 2000)
	private String description;

	@NotNull
	private VisibilityLevel visibility;

	@NotNull
	private LocalDate startDate;

	@NotNull
	private LocalDate endDate;

	private List<@Valid ReservationRequest> reservations;
}
