package com.appsBuild.club_management_system.dto.post;

import java.util.List;

public record PostListResponse(
    List<PostResponse> posts,
    PaginationResponse pagination
) {}
