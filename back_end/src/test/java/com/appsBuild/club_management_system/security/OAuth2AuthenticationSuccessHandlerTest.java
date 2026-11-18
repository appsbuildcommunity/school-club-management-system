package com.appsBuild.club_management_system.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.OAuthProvider;
import com.appsBuild.club_management_system.service.OAuthAccountService;
import com.appsBuild.club_management_system.service.RefreshTokenService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthenticationSuccessHandlerTest {

  private static final String REDIRECT_URI = "http://localhost:5173/auth/callback";

  private final OAuthAccountService oauthAccountService = mock(OAuthAccountService.class);
  private final JwtService jwtService = mock(JwtService.class);
  private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);

  private OAuth2AuthenticationSuccessHandler handler;

  @BeforeEach
  void setUp() {
    handler =
        new OAuth2AuthenticationSuccessHandler(
            oauthAccountService, jwtService, refreshTokenService);
    ReflectionTestUtils.setField(handler, "redirectUri", REDIRECT_URI);
  }

  @Test
  void googleSignIn_issuesTokenAndRedirectsWithFragment() throws Exception {
    User user = User.builder().userId(5L).role("USER").build();
    when(oauthAccountService.signIn(
            OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe"))
        .thenReturn(user);
    when(jwtService.issueAccessToken(user)).thenReturn("access-token");
    when(jwtService.accessTtlSeconds()).thenReturn(3600L);
    when(refreshTokenService.issue(user)).thenReturn("refresh-token");

    OidcUser principal = mock(OidcUser.class);
    when(principal.getSubject()).thenReturn("google-sub");
    when(principal.getEmail()).thenReturn("alice@example.com");
    when(principal.getEmailVerified()).thenReturn(true);
    when(principal.getFullName()).thenReturn("Alice Doe");
    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(principal, List.of(), "google");

    MockHttpServletResponse response = new MockHttpServletResponse();
    handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

    assertEquals(
        REDIRECT_URI
            + "#access_token=access-token&refresh_token=refresh-token&token_type=Bearer&expires_in=3600"
            + "&onboarding_required=true",
        response.getRedirectedUrl());
  }

  @Test
  void returningUser_redirectsWithoutOnboarding() throws Exception {
    User user = User.builder().userId(5L).role("USER").onboardingCompleted(true).build();
    when(oauthAccountService.signIn(
            OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe"))
        .thenReturn(user);
    when(jwtService.issueAccessToken(user)).thenReturn("access-token");
    when(jwtService.accessTtlSeconds()).thenReturn(3600L);
    when(refreshTokenService.issue(user)).thenReturn("refresh-token");

    OidcUser principal = mock(OidcUser.class);
    when(principal.getSubject()).thenReturn("google-sub");
    when(principal.getEmail()).thenReturn("alice@example.com");
    when(principal.getEmailVerified()).thenReturn(true);
    when(principal.getFullName()).thenReturn("Alice Doe");
    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(principal, List.of(), "google");

    MockHttpServletResponse response = new MockHttpServletResponse();
    handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

    assertEquals(
        REDIRECT_URI
            + "#access_token=access-token&refresh_token=refresh-token&token_type=Bearer&expires_in=3600"
            + "&onboarding_required=false",
        response.getRedirectedUrl());
  }

  @Test
  void facebookSignIn_usesOAuth2AttributesAndUnverifiedEmail() throws Exception {
    User user = User.builder().userId(6L).role("USER").build();
    when(oauthAccountService.signIn(
            OAuthProvider.FACEBOOK, "fb-id", "alice@example.com", false, "Alice Doe"))
        .thenReturn(user);
    when(jwtService.issueAccessToken(user)).thenReturn("facebook-token");
    when(jwtService.accessTtlSeconds()).thenReturn(600L);
    when(refreshTokenService.issue(user)).thenReturn("facebook-refresh-token");

    DefaultOAuth2User principal =
        new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of("id", "fb-id", "email", "alice@example.com", "name", "Alice Doe"),
            "id");
    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "facebook");

    MockHttpServletResponse response = new MockHttpServletResponse();
    handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

    assertEquals(
        REDIRECT_URI
            + "#access_token=facebook-token&refresh_token=facebook-refresh-token&token_type=Bearer&expires_in=600"
            + "&onboarding_required=true",
        response.getRedirectedUrl());
  }

  @Test
  void accountConflict_redirectsWithErrorInsteadOfThrowing() throws Exception {
    when(oauthAccountService.signIn(
            OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe"))
        .thenThrow(new ConflictException("An account with this email already exists."));

    OidcUser principal = mock(OidcUser.class);
    when(principal.getSubject()).thenReturn("google-sub");
    when(principal.getEmail()).thenReturn("alice@example.com");
    when(principal.getEmailVerified()).thenReturn(true);
    when(principal.getFullName()).thenReturn("Alice Doe");
    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(principal, List.of(), "google");

    MockHttpServletResponse response = new MockHttpServletResponse();
    handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

    String redirect = response.getRedirectedUrl();
    assertTrue(redirect.startsWith(REDIRECT_URI + "#error=account_error"));
    assertTrue(redirect.contains("error_description="));
  }
}
