package com.appsBuild.club_management_system.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appsBuild.club_management_system.config.JwtConfig;
import com.appsBuild.club_management_system.model.entity.User;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class JwtServiceTest {

  private static final String SECRET = "test-secret-test-secret-test-secret-1234";
  private static final String ISSUER = "test-issuer";
  private static final String AUDIENCE = "test-audience";

  private final JwtConfig config = new JwtConfig();
  private final JwtEncoder encoder = config.jwtEncoder(SECRET);
  private final JwtDecoder decoder = config.jwtDecoder(SECRET, ISSUER, AUDIENCE);
  private final JwtService service =
      new JwtService(encoder, Duration.ofMinutes(15), ISSUER, AUDIENCE);

  @Test
  void issueAccessToken_containsLocalUserIdentityAndRole() {
    User user = User.builder().userId(42L).role("ADMIN").build();

    String token = service.issueAccessToken(user);
    Jwt jwt = decoder.decode(token);

    assertEquals("42", jwt.getSubject());
    assertEquals(List.of("ADMIN"), jwt.getClaimAsStringList("roles"));
    assertEquals(ISSUER, jwt.getClaimAsString("iss"));
    assertNotNull(jwt.getAudience());
    assertTrue(jwt.getAudience().contains(AUDIENCE));
    assertNotNull(jwt.getExpiresAt());
    assertNotNull(jwt.getId());
  }

  @Test
  void issueAccessToken_acceptsLocalUserIdAndRole() {
    String token = service.issueAccessToken(42L, "STUDENT");
    Jwt jwt = decoder.decode(token);

    assertEquals("42", jwt.getSubject());
    assertEquals(List.of("STUDENT"), jwt.getClaimAsStringList("roles"));
    assertEquals(ISSUER, jwt.getClaimAsString("iss"));
  }

  @Test
  void jwtAuthenticationConverter_mapsLocalRoleToSpringAuthority() {
    User user = User.builder().userId(42L).role("STUDENT").build();
    Jwt jwt = decoder.decode(service.issueAccessToken(user));

    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) config.jwtAuthenticationConverter().convert(jwt);

    assertTrue(
        authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT")));
    assertEquals("42", authentication.getName());
  }

  @Test
  void accessTtlSeconds_reflectsConfiguredDuration() {
    assertEquals(900, service.accessTtlSeconds());
  }
}
