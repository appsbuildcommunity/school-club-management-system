package com.appsBuild.club_management_system.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthenticationFailureHandlerTest {

  private static final String REDIRECT_URI = "http://localhost:5173/auth/callback";

  private final OAuth2AuthenticationFailureHandler handler =
      new OAuth2AuthenticationFailureHandler();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(handler, "redirectUri", REDIRECT_URI);
  }

  @Test
  void oauth2Failure_redirectsWithProviderErrorCodeAndDescription() throws Exception {
    OAuth2AuthenticationException exception =
        new OAuth2AuthenticationException(
            new OAuth2Error("access_denied", "User denied access", null));

    MockHttpServletResponse response = new MockHttpServletResponse();
    handler.onAuthenticationFailure(new MockHttpServletRequest(), response, exception);

    assertEquals(
        REDIRECT_URI + "#error=access_denied&error_description=User%20denied%20access",
        response.getRedirectedUrl());
  }

  @Test
  void genericFailure_redirectsWithOpaqueError() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    handler.onAuthenticationFailure(
        new MockHttpServletRequest(), response, new BadCredentialsException("secret detail"));

    String redirect = response.getRedirectedUrl();
    assertTrue(redirect.startsWith(REDIRECT_URI + "#error=oauth_failed"));
    assertTrue(redirect.contains("error_description=Sign-in%20failed"));
    assertTrue(!redirect.contains("secret%20detail"));
  }
}
