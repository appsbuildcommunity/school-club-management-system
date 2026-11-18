package com.appsBuild.club_management_system.dto.post;

import com.appsBuild.club_management_system.model.enums.AttachmentType;

public record AttachmentResponse(
    Long attachmentId,
    AttachmentType type,
    String url
) {}
