package com.appsBuild.club_management_system.dto.club;

import java.util.List;

public record ClubProfileResponse(Long profileId, String name, List<String> endpoints) {}
