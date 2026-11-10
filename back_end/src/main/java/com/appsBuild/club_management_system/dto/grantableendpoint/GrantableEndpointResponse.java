package com.appsBuild.club_management_system.dto.grantableendpoint;

import com.appsBuild.club_management_system.model.enums.Category;

public record GrantableEndpointResponse(
    String name, String description, Category category, boolean privileged) {}
