package com.appsBuild.club_management_system.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakWebhookEvent(
    String type,
    @JsonProperty("userId") String userId,
    @JsonProperty("details") Details details) {

  public record Details(
      String username,
      String email,
      @JsonProperty("first_name") String firstName,
      @JsonProperty("last_name") String lastName) {}
}
