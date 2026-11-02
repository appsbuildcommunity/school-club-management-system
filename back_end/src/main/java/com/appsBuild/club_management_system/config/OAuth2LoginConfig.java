package com.appsBuild.club_management_system.config;

import com.appsBuild.club_management_system.security.OAuth2AuthenticationFailureHandler;
import com.appsBuild.club_management_system.security.OAuth2AuthenticationSuccessHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * OAuth2 client login.
 *
 * <p>Registrations are only created for providers with a configured client id and secret. The
 * sign-in UI is owned by the frontend; this chain only handles the provider handshake and the
 * back-end callback before redirecting to the configured frontend URL.
 */
@Slf4j
@Configuration
public class OAuth2LoginConfig {

  private final OAuth2AuthenticationSuccessHandler successHandler;
  private final OAuth2AuthenticationFailureHandler failureHandler;

  private final String googleClientId;
  private final String googleClientSecret;
  private final String facebookClientId;
  private final String facebookClientSecret;
  private final String facebookGraphVersion;
  private final String redirectBaseUrl;

  public OAuth2LoginConfig(
      OAuth2AuthenticationSuccessHandler successHandler,
      OAuth2AuthenticationFailureHandler failureHandler,
      @Value("${app.oauth.google.client-id:}") String googleClientId,
      @Value("${app.oauth.google.client-secret:}") String googleClientSecret,
      @Value("${app.oauth.facebook.client-id:}") String facebookClientId,
      @Value("${app.oauth.facebook.client-secret:}") String facebookClientSecret,
      @Value("${app.oauth.facebook.graph-version}") String facebookGraphVersion,
      @Value("${app.oauth.redirect-base-url}") String redirectBaseUrl) {
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
    this.googleClientId = googleClientId;
    this.googleClientSecret = googleClientSecret;
    this.facebookClientId = facebookClientId;
    this.facebookClientSecret = facebookClientSecret;
    this.facebookGraphVersion = facebookGraphVersion;
    this.redirectBaseUrl = redirectBaseUrl;
  }

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    Map<String, ClientRegistration> registrations = new LinkedHashMap<>();
    if (StringUtils.hasText(googleClientId) && StringUtils.hasText(googleClientSecret)) {
      registrations.put("google", googleRegistration());
    }
    if (StringUtils.hasText(facebookClientId) && StringUtils.hasText(facebookClientSecret)) {
      registrations.put("facebook", facebookRegistration());
    }
    if (registrations.isEmpty()) {
      log.warn(
          "No OAuth2 providers are configured. Set GOOGLE_CLIENT_ID/SECRET or FACEBOOK_CLIENT_ID/SECRET.");
    }
    return registrations::get;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain oauth2LoginSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/oauth2/**", "/login/**")
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .oauth2Login(
            oauth2 ->
                oauth2.successHandler(successHandler).failureHandler(failureHandler));
    return http.build();
  }

  private ClientRegistration googleRegistration() {
    return CommonOAuth2Provider.GOOGLE
        .getBuilder("google")
        .clientId(googleClientId)
        .clientSecret(googleClientSecret)
        .redirectUri(callbackUrl("google"))
        .scope("openid", "profile", "email")
        .clientName("Google")
        .build();
  }

  private ClientRegistration facebookRegistration() {
    String graphBase = "https://graph.facebook.com/" + facebookGraphVersion;
    return CommonOAuth2Provider.FACEBOOK
        .getBuilder("facebook")
        .clientId(facebookClientId)
        .clientSecret(facebookClientSecret)
        .redirectUri(callbackUrl("facebook"))
        .scope("email", "public_profile")
        .authorizationUri("https://www.facebook.com/" + facebookGraphVersion + "/dialog/oauth")
        .tokenUri(graphBase + "/oauth/access_token")
        .userInfoUri(graphBase + "/me?fields=id,name,email")
        .userNameAttributeName("id")
        .clientName("Facebook")
        .build();
  }

  private String callbackUrl(String registrationId) {
    String base =
        redirectBaseUrl.endsWith("/")
            ? redirectBaseUrl.substring(0, redirectBaseUrl.length() - 1)
            : redirectBaseUrl;
    return base + "/login/oauth2/code/" + registrationId;
  }
}
