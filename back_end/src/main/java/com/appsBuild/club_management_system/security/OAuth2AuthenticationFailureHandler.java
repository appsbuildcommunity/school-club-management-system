package com.appsBuild.club_management_system.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/** Sends the browser back to the client with a readable error instead of a bare /login?error. */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Value("${app.auth.redirect-uri}")
  private String redirectUri;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    log.warn("OAuth2 authentication failed: {}", exception.getMessage());
    String errorCode = "oauth_failed";
    String message = "Sign-in failed. Please try again.";
    if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
      if (StringUtils.hasText(oauth2Exception.getError().getErrorCode())) {
        errorCode = oauth2Exception.getError().getErrorCode();
      }
      if (StringUtils.hasText(oauth2Exception.getError().getDescription())) {
        message = oauth2Exception.getError().getDescription();
      }
    }
    String target =
        UriComponentsBuilder.fromUriString(redirectUri)
            .fragment("error={error}&error_description={description}")
            .encode()
            .buildAndExpand(errorCode, message)
            .toUriString();
    response.sendRedirect(target);
  }
}
