package com.appsBuild.club_management_system.dto.s3;

public record S3UpdateResponse(String uploadUrl, String oldKey, String newKey) {}
