package com.appsBuild.club_management_system.dto.privilege;

import java.util.Date;
import java.util.List;

public record MemberPrivilege(
    String endpointName, Date grantedDate, List<SourceProfile> sources) {}
