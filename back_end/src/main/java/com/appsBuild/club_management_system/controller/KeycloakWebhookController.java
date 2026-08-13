package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.keycloak.KeycloakWebhookEvent;
import com.appsBuild.club_management_system.service.keycloak.KeycloakWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/keycloak")
@RequiredArgsConstructor
public class KeycloakWebhookController {

  private final KeycloakWebhookService keycloakWebhookService;

  @PostMapping({"/user-registered", "/user-registered/"})
  public ResponseEntity<Void> userRegistered(@RequestBody KeycloakWebhookEvent event) {
    keycloakWebhookService.handleEvent(event);
    return ResponseEntity.ok().build();
  }
}
