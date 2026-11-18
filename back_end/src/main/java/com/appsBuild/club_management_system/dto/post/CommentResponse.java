package com.appsBuild.club_management_system.dto.post;

import java.util.Date;

public record CommentResponse(
    Long commentId,
    String content,
    UserReferenceResponse user,
    Date createdAt
) {}
