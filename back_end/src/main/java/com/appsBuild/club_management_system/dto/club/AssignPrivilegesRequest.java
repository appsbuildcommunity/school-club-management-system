package com.appsBuild.club_management_system.dto.club;

import java.util.List;

public record AssignPrivilegesRequest(Long profileId, List<String> endpoints) {}
