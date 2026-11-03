package com.appsBuild.club_management_system.dto.club;

import jakarta.validation.constraints.AssertTrue;
import java.util.List;

public record AssignPrivilegesRequest(Long profileId, List<String> endpoints) {

  // Either assign from a profile, or grant individual endpoints — never neither.
  @AssertTrue(message = "either profileId or a non-empty endpoints list must be provided")
  boolean hasAssignmentTarget() {
    return profileId != null || (endpoints != null && !endpoints.isEmpty());
  }
}
