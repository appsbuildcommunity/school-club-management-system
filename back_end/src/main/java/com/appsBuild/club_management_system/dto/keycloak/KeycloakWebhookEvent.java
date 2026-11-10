package com.appsBuild.club_management_system.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KeycloakWebhookEvent(
    @NotBlank String type,
    @JsonProperty("userId") @NotBlank String userId,
    @JsonProperty("details") @NotNull @Valid Details details) {

  public record Details(
      @JsonProperty("username") @NotBlank String username,
      @JsonProperty("email") @NotBlank String email,
      @JsonProperty("first_name") @NotBlank String firstName,
      @JsonProperty("last_name") @NotBlank String lastName) {}
}
