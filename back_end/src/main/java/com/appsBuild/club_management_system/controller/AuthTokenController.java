package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.auth.RefreshTokenRequest;
import com.appsBuild.club_management_system.dto.auth.TokenResponse;
import com.appsBuild.club_management_system.security.JwtService;
import com.appsBuild.club_management_system.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthTokenController {

  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
      @Valid @RequestBody RefreshTokenRequest request) {
    RefreshTokenService.RefreshResult result = refreshTokenService.rotate(request.refreshToken());
    String accessToken = jwtService.issueAccessToken(result.userId(), result.role());
    return ResponseEntity.ok(
        new TokenResponse(
            accessToken, result.refreshToken(), "Bearer", jwtService.accessTtlSeconds()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
    refreshTokenService.revoke(request.refreshToken());
    return ResponseEntity.noContent().build();
  }
}
