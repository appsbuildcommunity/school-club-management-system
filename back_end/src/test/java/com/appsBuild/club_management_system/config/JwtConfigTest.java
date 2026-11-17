package com.appsBuild.club_management_system.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class JwtConfigTest {

  private static final String PLACEHOLDER = "dev-only-secret-change-me-0123456789abcdef";

  private final JwtConfig config = new JwtConfig();

  @Test
  void jwtEncoder_publicPlaceholderSecret_isRejected() {
    assertThrows(IllegalStateException.class, () -> config.jwtEncoder(PLACEHOLDER));
    assertThrows(IllegalStateException.class, () -> config.jwtEncoder("  " + PLACEHOLDER + " "));
  }

  @Test
  void jwtEncoder_nullOrBlankSecret_isRejected() {
    assertThrows(IllegalStateException.class, () -> config.jwtEncoder(null));
    assertThrows(IllegalStateException.class, () -> config.jwtEncoder(""));
    assertThrows(IllegalStateException.class, () -> config.jwtEncoder("   "));
  }

  @Test
  void jwtEncoder_shortSecret_isRejected() {
    assertThrows(IllegalStateException.class, () -> config.jwtEncoder("a".repeat(31)));
  }

  @Test
  void jwtEncoder_validSecret_works() {
    assertDoesNotThrow(() -> config.jwtEncoder("a".repeat(32)));
    assertDoesNotThrow(() -> config.jwtEncoder("some-other-secret-that-is-long-enough-0123"));
  }

  @Test
  void jwtConfig_hasNoConfiguredSecretField() {
    assertTrue(
        Arrays.stream(JwtConfig.class.getDeclaredFields())
            .noneMatch(f -> f.getName().equals("configuredSecret")));
  }

  @Test
  void applicationProperties_jwtSecretHasNoDefault() throws Exception {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
      String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(
          content.lines().anyMatch(l -> l.trim().equals("app.jwt.secret=${JWT_SECRET}")),
          "app.jwt.secret must have no default");
    }
  }
}
