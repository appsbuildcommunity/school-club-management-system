package com.appsBuild.club_management_system.controller;

import java.util.Map;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lets the frontend hide providers that have no configured credentials. */
@RestController
@RequestMapping("/api/auth")
public class AuthProvidersController {

  private final ClientRegistrationRepository clientRegistrationRepository;

  public AuthProvidersController(ClientRegistrationRepository clientRegistrationRepository) {
    this.clientRegistrationRepository = clientRegistrationRepository;
  }

  @GetMapping("/providers")
  public Map<String, Boolean> providers() {
    return Map.of(
        "google", clientRegistrationRepository.findByRegistrationId("google") != null,
        "facebook", clientRegistrationRepository.findByRegistrationId("facebook") != null);
  }
}
