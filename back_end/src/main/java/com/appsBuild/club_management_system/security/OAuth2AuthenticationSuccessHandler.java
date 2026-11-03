package com.appsBuild.club_management_system.security;

import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.OAuthProvider;
import com.appsBuild.club_management_system.service.OAuthAccountService;
import com.appsBuild.club_management_system.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Turns a successful Google/Facebook login into a local user and locally issued access and refresh
 * tokens.
 *
 * <p>The tokens are placed in the URL fragment so they are not sent to any server or included in
 * the Referer header. A production web client should exchange a short-lived one-time code instead.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final OAuthAccountService oauthAccountService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  @Value("${app.auth.redirect-uri}")
  private String redirectUri;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    try {
      if (!(authentication instanceof OAuth2AuthenticationToken token)) {
        throw new ApplicationException("Unsupported authentication type");
      }
      OAuthProvider provider =
          OAuthProvider.fromRegistrationId(token.getAuthorizedClientRegistrationId());
      OAuthUserInfo info = extractUserInfo(token.getPrincipal());
      User user =
          oauthAccountService.signIn(
              provider, info.subject(), info.email(), info.emailVerified(), info.displayName());
      String accessToken = jwtService.issueAccessToken(user);
      String refreshToken = refreshTokenService.issue(user);
      String target =
          UriComponentsBuilder.fromUriString(redirectUri)
              .fragment(
                  "access_token={token}&refresh_token={refreshToken}"
                      + "&token_type=Bearer&expires_in={expiresIn}&onboarding_required={onboardingRequired}")
              .encode()
              .buildAndExpand(
                  accessToken, refreshToken, jwtService.accessTtlSeconds(), !user.isOnboardingCompleted())
              .toUriString();
      response.sendRedirect(target);
    } catch (ApplicationException ex) {
      redirectError(response, "account_error", ex.getMessage());
    } catch (Exception ex) {
      redirectError(response, "sign_in_failed", "Sign-in could not be completed.");
    }
  }

  private OAuthUserInfo extractUserInfo(Object principal) {
    if (principal instanceof OidcUser oidcUser) {
      return new OAuthUserInfo(
          oidcUser.getSubject(),
          oidcUser.getEmail(),
          Boolean.TRUE.equals(oidcUser.getEmailVerified()),
          oidcUser.getFullName());
    }
    if (principal instanceof OAuth2User oauth2User) {
      return new OAuthUserInfo(
          oauth2User.getAttribute("id"),
          oauth2User.getAttribute("email"),
          false,
          oauth2User.getAttribute("name"));
    }
    throw new ApplicationException("Unsupported OAuth2 principal");
  }

  private void redirectError(HttpServletResponse response, String error, String description)
      throws IOException {
    String target =
        UriComponentsBuilder.fromUriString(redirectUri)
            .fragment("error={error}&error_description={description}")
            .encode()
            .buildAndExpand(error, description == null ? "Sign-in failed" : description)
            .toUriString();
    response.sendRedirect(target);
  }

  private record OAuthUserInfo(
      String subject, String email, boolean emailVerified, String displayName) {}
}
