package com.appsBuild.club_management_system.dto.s3Services.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UploadDtoResponse {
  private String uploadUrl;
  private String key;
}
