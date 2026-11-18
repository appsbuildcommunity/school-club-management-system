package com.appsBuild.club_management_system.dto.event;

import java.util.List;

import com.appsBuild.club_management_system.dto.common.Pagination;

public record EventPageResponse(List<EventResponse> events, Pagination pagination) {}
