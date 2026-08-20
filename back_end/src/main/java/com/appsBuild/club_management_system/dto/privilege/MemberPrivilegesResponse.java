package com.appsBuild.club_management_system.dto.privilege;

import java.util.List;

public record MemberPrivilegesResponse(
    Long membershipId, List<MemberPrivilege> privileges) {}
