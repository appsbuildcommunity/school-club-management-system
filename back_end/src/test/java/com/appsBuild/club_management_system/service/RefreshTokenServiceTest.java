package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsBuild.club_management_system.exception.impl.UnauthorizedException;
import com.appsBuild.club_management_system.model.entity.RefreshToken;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  private static final User USER = User.builder().userId(1L).role("USER").build();

  @Mock private RefreshTokenRepository repository;

  private RefreshTokenService service;

  @BeforeEach
  void setUp() {
    service = new RefreshTokenService(repository, Duration.ofDays(30));
  }

  @Test
  void issue_storesOnlyTheHashAndSetsExpiry() {
    when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

    String rawToken = service.issue(USER);

    assertNotNull(rawToken);
    assertTrue(rawToken.matches("[A-Za-z0-9_-]+"));
    assertNotEquals(rawToken, sha256(rawToken));

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(repository).save(captor.capture());
    RefreshToken saved = captor.getValue();
    assertEquals(sha256(rawToken), saved.getTokenHash());
    assertEquals(USER, saved.getUser());
    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getExpiresAt());
    assertNull(saved.getRevokedAt());
    assertEquals(30, Duration.between(
        saved.getCreatedAt().toInstant(), saved.getExpiresAt().toInstant()).toDays());
  }

  @Test
  void rotate_revokesTheOldTokenAndReturnsANewOne() {
    RefreshToken existing = refreshToken("old", USER, Instant.now().plusSeconds(3600), null);
    when(repository.findByTokenHashForUpdate(sha256("old"))).thenReturn(Optional.of(existing));
    when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RefreshTokenService.RefreshResult result = service.rotate("old");

    assertEquals(1L, result.userId());
    assertEquals("USER", result.role());
    assertNotEquals("old", result.refreshToken());
    assertNotNull(existing.getRevokedAt());

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(repository, times(2)).save(captor.capture());
    RefreshToken newToken =
        captor.getAllValues().stream().filter(token -> token != existing).findFirst().orElseThrow();
    assertEquals(sha256(result.refreshToken()), newToken.getTokenHash());
    assertNull(newToken.getRevokedAt());
  }

  @Test
  void rotate_rejectsAnUnknownToken() {
    when(repository.findByTokenHashForUpdate(sha256("missing"))).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> service.rotate("missing"));
    verify(repository, never()).save(any());
  }

  @Test
  void rotate_rejectsABlankToken() {
    assertThrows(UnauthorizedException.class, () -> service.rotate(" "));
    verify(repository, never()).findByTokenHashForUpdate(any());
  }

  @Test
  void rotate_rejectsAnExpiredTokenAndRevokesIt() {
    RefreshToken expired = refreshToken("expired", USER, Instant.now().minusSeconds(1), null);
    when(repository.findByTokenHashForUpdate(sha256("expired"))).thenReturn(Optional.of(expired));

    assertThrows(UnauthorizedException.class, () -> service.rotate("expired"));
    assertNotNull(expired.getRevokedAt());
    verify(repository).save(expired);
  }

  @Test
  void rotate_revokesEveryActiveTokenWhenARevokedTokenIsReused() {
    RefreshToken reused = refreshToken("reused", USER, Instant.now().plusSeconds(3600), new Date());
    RefreshToken other = refreshToken("other", USER, Instant.now().plusSeconds(3600), null);
    when(repository.findByTokenHashForUpdate(sha256("reused"))).thenReturn(Optional.of(reused));
    when(repository.findByUser_UserIdAndRevokedAtIsNull(1L)).thenReturn(List.of(other));

    assertThrows(UnauthorizedException.class, () -> service.rotate("reused"));
    assertNotNull(other.getRevokedAt());
    verify(repository).saveAll(List.of(other));
  }

  @Test
  void revoke_marksTheTokenAsRevoked() {
    RefreshToken existing = refreshToken("old", USER, Instant.now().plusSeconds(3600), null);
    when(repository.findByTokenHashForUpdate(sha256("old"))).thenReturn(Optional.of(existing));

    service.revoke("old");

    assertNotNull(existing.getRevokedAt());
    verify(repository).save(existing);
  }

  @Test
  void revoke_isIdempotentForUnknownTokens() {
    when(repository.findByTokenHashForUpdate(sha256("missing"))).thenReturn(Optional.empty());

    service.revoke("missing");

    verify(repository, never()).save(any());
  }

  @Test
  void revoke_doesNotTouchAnAlreadyRevokedToken() {
    RefreshToken existing = refreshToken("old", USER, Instant.now().plusSeconds(3600), new Date());
    when(repository.findByTokenHashForUpdate(sha256("old"))).thenReturn(Optional.of(existing));

    service.revoke("old");

    verify(repository, never()).save(any());
  }

  @Test
  void refreshTtlSeconds_reflectsConfiguredDuration() {
    assertEquals(30L * 24 * 60 * 60, service.refreshTtlSeconds());
  }

  private RefreshToken refreshToken(
      String rawToken, User user, Instant expiresAt, Date revokedAt) {
    return RefreshToken.builder()
        .user(user)
        .tokenHash(sha256(rawToken))
        .createdAt(new Date())
        .expiresAt(Date.from(expiresAt))
        .revokedAt(revokedAt)
        .build();
  }

  private static String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}
