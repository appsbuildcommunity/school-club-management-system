package com.appsBuild.club_management_system.model.enums;

import com.appsBuild.club_management_system.exception.ApplicationException;
import java.util.Arrays;

public enum OAuthProvider {
  GOOGLE("google"),
  FACEBOOK("facebook");

  private final String registrationId;

  OAuthProvider(String registrationId) {
    this.registrationId = registrationId;
  }

  public String registrationId() {
    return registrationId;
  }

  public static OAuthProvider fromRegistrationId(String registrationId) {
    return Arrays.stream(values())
        .filter(provider -> provider.registrationId.equals(registrationId))
        .findFirst()
        .orElseThrow(
            () -> new ApplicationException("Unsupported OAuth provider: " + registrationId));
  }
}
