package com.appsBuild.club_management_system.dto.post;

public record PaginationResponse(
    int currentPage,
    int totalPages,
    long totalItems
) {}
