package com.appsBuild.club_management_system.security;

import com.appsBuild.club_management_system.model.entity.User;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Issues local access tokens for users that have completed an OAuth2 sign-in. */
@Service
public class JwtService {

  private final JwtEncoder encoder;
  private final Duration accessTtl;
  private final String issuer;
  private final String audience;

  public JwtService(
      JwtEncoder encoder,
      @Value("${app.jwt.access-ttl}") Duration accessTtl,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.audience}") String audience) {
    this.encoder = encoder;
    this.accessTtl = accessTtl;
    this.issuer = issuer;
    this.audience = audience;
  }

  public String issueAccessToken(User user) {
    return issueAccessToken(user.getUserId(), user.getRole());
  }

  public String issueAccessToken(Long userId, String role) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .audience(List.of(audience))
            .issuedAt(now)
            .expiresAt(now.plus(accessTtl))
            .subject(String.valueOf(userId))
            .claim("roles", List.of(role))
            .id(UUID.randomUUID().toString())
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  public long accessTtlSeconds() {
    return accessTtl.toSeconds();
  }
}
