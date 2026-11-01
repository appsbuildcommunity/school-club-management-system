package com.appsBuild.club_management_system.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsBuild.club_management_system.dto.s3.S3UploadResponse;
import com.appsBuild.club_management_system.dto.user.UpdateRoleRequest;
import com.appsBuild.club_management_system.dto.user.UserClubResponse;
import com.appsBuild.club_management_system.dto.user.UserClubsResponse;
import com.appsBuild.club_management_system.dto.user.UserResponse;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.service.ClubAccessService;
import com.appsBuild.club_management_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableWebSecurity
@EnableMethodSecurity
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private UserService userService;
  @MockitoBean(name = "clubAccess") private ClubAccessService clubAccessService;

  @BeforeEach
  void setUp() {
    authenticate("USER");
  }

  private void authenticate(String... roles) {
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "test-user")
            .build();
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
    }
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt, authorities));
  }

  // ── getCurrentUser ──────────────────────────────────────────────────

  @Test
  void getCurrentUser_returns200() throws Exception {
    when(userService.getCurrentUser())
        .thenReturn(
            new UserResponse(
                1L, "alice", "alice@example.com", "Alice", "Doe", null, List.of("USER")));

    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.username").value("alice"))
        .andExpect(jsonPath("$.profilePictureUrl").doesNotExist())
        .andExpect(jsonPath("$.roles[0]").value("USER"));
  }

  // ── updateRole ──────────────────────────────────────────────────────

  @Test
  void updateRole_asAdmin_returns200() throws Exception {
    authenticate("ADMIN");

    mockMvc
        .perform(
            patch("/api/users/5/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateRoleRequest("STUDENT"))))
        .andExpect(status().isOk());

    verify(userService).updateRole(5L, "STUDENT");
  }

  @Test
  void updateRole_asCoordinationPresident_returns200() throws Exception {
    when(clubAccessService.hasCoordinationEndpoint("change_user_role")).thenReturn(true);

    mockMvc
        .perform(
            patch("/api/users/5/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateRoleRequest("USER"))))
        .andExpect(status().isOk());

    verify(userService).updateRole(5L, "USER");
  }

  @Test
  void updateRole_withoutPrivilege_returns403() throws Exception {
    when(clubAccessService.hasCoordinationEndpoint("change_user_role")).thenReturn(false);

    mockMvc
        .perform(
            patch("/api/users/5/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateRoleRequest("STUDENT"))))
        .andExpect(status().isForbidden());

    verify(userService, never()).updateRole(anyLong(), any());
  }

  @Test
  void updateRole_blankRole_returns400() throws Exception {
    authenticate("ADMIN");

    mockMvc
        .perform(
            patch("/api/users/5/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateRoleRequest(""))))
        .andExpect(status().isBadRequest());

    verify(userService, never()).updateRole(anyLong(), any());
  }

  // ── getProfilePictureUploadUrl ──────────────────────────────────────

  @Test
  void getProfilePictureUploadUrl_returns200() throws Exception {
    when(userService.requestProfilePictureUpload("pic.png"))
        .thenReturn(new S3UploadResponse("https://put", "users/1/profile-picture/pic.png"));

    mockMvc
        .perform(get("/api/users/me/profile-picture/upload-url").param("name", "pic.png"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uploadUrl").value("https://put"))
        .andExpect(jsonPath("$.key").value("users/1/profile-picture/pic.png"));
  }

  @Test
  void getProfilePictureUploadUrl_missingName_returns400() throws Exception {
    mockMvc
        .perform(get("/api/users/me/profile-picture/upload-url"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(userService);
  }

  // ── getCurrentUserClubs ─────────────────────────────────────────────

  @Test
  void getCurrentUserClubs_returns200() throws Exception {
    when(userService.getCurrentUserClubs())
        .thenReturn(
            new UserClubsResponse(
                List.of(
                    new UserClubResponse(
                        5L,
                        "Chess Club",
                        "Chess Lovers",
                        "Play chess",
                        false,
                        "https://s3/chess.png",
                        ClubRole.MEMBER,
                        new Date(1_759_000_000_000L)))));

    mockMvc
        .perform(get("/api/users/me/clubs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberships.length()").value(1))
        .andExpect(jsonPath("$.memberships[0].membershipId").value(5))
        .andExpect(jsonPath("$.memberships[0].clubName").value("Chess Club"))
        .andExpect(jsonPath("$.memberships[0].isCoordinationClub").value(false))
        .andExpect(jsonPath("$.memberships[0].clubRole").value("MEMBER"))
        .andExpect(jsonPath("$.memberships[0].profilePictureUrl").value("https://s3/chess.png"))
        .andExpect(jsonPath("$.memberships[0].joinedDate").exists());
  }

  @Test
  void getCurrentUserClubs_whenEmpty_returns200() throws Exception {
    when(userService.getCurrentUserClubs()).thenReturn(new UserClubsResponse(List.of()));

    mockMvc
        .perform(get("/api/users/me/clubs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberships").isEmpty());
  }
}
