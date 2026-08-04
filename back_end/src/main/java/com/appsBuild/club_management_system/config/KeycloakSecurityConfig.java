package com.appsBuild.club_management_system.config;

import com.appsBuild.club_management_system.security.KeycloakJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Profile("!Test")
public class KeycloakSecurityConfig {

  @Value("${keycloak.webhook.username}")
  private String webhookUsername;

  @Value("${keycloak.webhook.password}")
  private String webhookPassword;

  @Bean
  @Order(1)
  public SecurityFilterChain keycloakWebhookSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/webhooks/keycloak/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/verify").permitAll().anyRequest().authenticated())
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtAuthenticationConverter());
    return converter;
  }

  @Bean
  public UserDetailsService webhookUserDetailsService() {
    UserDetails webhookUser =
        User.withUsername(webhookUsername)
            .password("{noop}" + webhookPassword)
            .authorities("WEBHOOK")
            .build();
    return new InMemoryUserDetailsManager(webhookUser);
  }
}
