package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.exception.impl.UnauthorizedException;
import com.appsBuild.club_management_system.model.entity.RefreshToken;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Issues, rotates, and revokes opaque refresh tokens.
 *
 * <p>Only a SHA-256 hash of each token is stored. Rotating a token revokes the old one, so a
 * refresh token can only be used once. Reuse of an already-revoked token revokes every active
 * token for that user.
 */
@Service
public class RefreshTokenService {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int TOKEN_BYTES = 32;

  private final RefreshTokenRepository repository;
  private final Duration refreshTtl;

  public RefreshTokenService(
      RefreshTokenRepository repository, @Value("${app.jwt.refresh-ttl}") Duration refreshTtl) {
    this.repository = repository;
    this.refreshTtl = refreshTtl;
  }

  @Transactional
  public String issue(User user) {
    return create(user);
  }

  @Transactional(noRollbackFor = UnauthorizedException.class)
  public RefreshResult rotate(String rawToken) {
    Instant now = Instant.now();
    RefreshToken token =
        repository
            .findByTokenHashForUpdate(hash(rawToken))
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

    if (token.getRevokedAt() != null) {
      revokeAllActiveFor(token.getUser().getUserId(), now);
      throw new UnauthorizedException("Refresh token has already been used");
    }
    if (!token.getExpiresAt().toInstant().isAfter(now)) {
      token.setRevokedAt(Date.from(now));
      repository.save(token);
      throw new UnauthorizedException("Refresh token expired");
    }

    token.setRevokedAt(Date.from(now));
    repository.save(token);

    User user = token.getUser();
    String newToken = create(user);
    return new RefreshResult(user.getUserId(), user.getRole(), newToken);
  }

  @Transactional
  public void revoke(String rawToken) {
    repository
        .findByTokenHashForUpdate(hash(rawToken))
        .filter(token -> token.getRevokedAt() == null)
        .ifPresent(
            token -> {
              token.setRevokedAt(new Date());
              repository.save(token);
            });
  }

  public long refreshTtlSeconds() {
    return refreshTtl.toSeconds();
  }

  private String create(User user) {
    Instant now = Instant.now();
    String rawToken = randomToken();
    repository.save(
        RefreshToken.builder()
            .user(user)
            .tokenHash(hash(rawToken))
            .createdAt(Date.from(now))
            .expiresAt(Date.from(now.plus(refreshTtl)))
            .build());
    return rawToken;
  }

  private void revokeAllActiveFor(Long userId, Instant now) {
    List<RefreshToken> active = repository.findByUser_UserIdAndRevokedAtIsNull(userId);
    active.forEach(token -> token.setRevokedAt(Date.from(now)));
    repository.saveAll(active);
  }

  private String randomToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hash(String rawToken) {
    if (!StringUtils.hasText(rawToken)) {
      throw new UnauthorizedException("Invalid refresh token");
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }

  public record RefreshResult(Long userId, String role, String refreshToken) {}
}
