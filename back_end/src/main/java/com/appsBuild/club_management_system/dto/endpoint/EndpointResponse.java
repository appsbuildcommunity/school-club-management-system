package com.appsBuild.club_management_system.dto.endpoint;

import com.appsBuild.club_management_system.model.enums.Category;

public record EndpointResponse(
    String name, String description, Category category, boolean privileged) {}
