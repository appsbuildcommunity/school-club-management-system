package com.appsBuild.club_management_system.dto.post;

import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import java.util.Date;
import java.util.List;

public record PostDetailResponse(
    Long postId,
    String title,
    String content,
    ClubReferenceResponse club,
    Date createdAt,
    VisibilityLevel visibility,
    List<AttachmentResponse> attachments,
    List<CommentResponse> comments
) {}
