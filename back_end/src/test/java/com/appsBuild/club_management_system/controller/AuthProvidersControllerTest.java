package com.appsBuild.club_management_system.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

class AuthProvidersControllerTest {

  private final ClientRegistrationRepository clientRegistrationRepository =
      mock(ClientRegistrationRepository.class);
  private final AuthProvidersController controller =
      new AuthProvidersController(clientRegistrationRepository);

  @Test
  void reportsWhichProvidersHaveCredentials() {
    when(clientRegistrationRepository.findByRegistrationId("google"))
        .thenReturn(mock(ClientRegistration.class));
    when(clientRegistrationRepository.findByRegistrationId("facebook")).thenReturn(null);

    assertEquals(Map.of("google", true, "facebook", false), controller.providers());
  }
}
