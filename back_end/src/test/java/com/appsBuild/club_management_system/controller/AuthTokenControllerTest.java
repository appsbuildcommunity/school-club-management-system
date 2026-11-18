package com.appsBuild.club_management_system.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appsBuild.club_management_system.exception.impl.UnauthorizedException;
import com.appsBuild.club_management_system.security.JwtService;
import com.appsBuild.club_management_system.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthTokenControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RefreshTokenService refreshTokenService;
  @MockitoBean private JwtService jwtService;

  @Test
  void refresh_returnsANewAccessTokenAndRotatedRefreshToken() throws Exception {
    when(refreshTokenService.rotate("old-refresh"))
        .thenReturn(new RefreshTokenService.RefreshResult(1L, "USER", "new-refresh"));
    when(jwtService.issueAccessToken(1L, "USER")).thenReturn("new-access");
    when(jwtService.accessTtlSeconds()).thenReturn(3600L);

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"old-refresh\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access"))
        .andExpect(jsonPath("$.refreshToken").value("new-refresh"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").value(3600));
  }

  @Test
  void refresh_rejectsARevokedTokenWith401() throws Exception {
    when(refreshTokenService.rotate("reused"))
        .thenThrow(new UnauthorizedException("Refresh token has already been used"));

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"reused\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Refresh token has already been used"));
  }

  @Test
  void refresh_requiresARefreshToken() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("refreshToken: refreshToken is required"));
  }

  @Test
  void logout_revokesTheTokenAndReturns204() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"old-refresh\"}"))
        .andExpect(status().isNoContent());

    verify(refreshTokenService).revoke("old-refresh");
  }

  @Test
  void logout_requiresARefreshToken() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }
}
