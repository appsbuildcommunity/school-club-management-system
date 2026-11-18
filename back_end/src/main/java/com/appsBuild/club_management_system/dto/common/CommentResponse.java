package com.appsBuild.club_management_system.dto.common;

import java.util.Date;

public record CommentResponse(
    Long commentId, String content, UserReference user, Date createdAt) {}
