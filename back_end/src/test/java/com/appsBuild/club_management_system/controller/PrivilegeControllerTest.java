package com.appsBuild.club_management_system.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsBuild.club_management_system.dto.club.AssignPrivilegesRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileRequest;
import com.appsBuild.club_management_system.dto.club.ClubProfileResponse;
import com.appsBuild.club_management_system.dto.privilege.MemberPrivilege;
import com.appsBuild.club_management_system.dto.privilege.MemberPrivilegesResponse;
import com.appsBuild.club_management_system.dto.privilege.SourceProfile;
import com.appsBuild.club_management_system.service.ClubAccessService;
import com.appsBuild.club_management_system.service.ClubProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PrivilegeController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableWebSecurity
class PrivilegeControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private ClubProfileService clubProfileService;
  @MockitoBean(name = "clubAccess") private ClubAccessService clubAccessService;

  @BeforeEach
  void setUp() {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .claim("sub", "test-user")
        .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt));
  }

  // ── createProfile ───────────────────────────────────────────────────

  @Test
  void createProfile_returns201() throws Exception {
    ClubProfileResponse resp = new ClubProfileResponse(1L, "Event Mgr", List.of("manage_events"));
    when(clubProfileService.createProfile(eq(1L), any(ClubProfileRequest.class))).thenReturn(resp);

    mockMvc
        .perform(
            post("/api/clubs/1/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ClubProfileRequest("Event Mgr", List.of("manage_events")))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.profileId").value(1))
        .andExpect(jsonPath("$.name").value("Event Mgr"));
  }

  // ── updateProfile ───────────────────────────────────────────────────

  @Test
  void updateProfile_returns200() throws Exception {
    ClubProfileResponse resp = new ClubProfileResponse(1L, "Updated", List.of("manage_events"));
    when(clubProfileService.updateProfile(eq(1L), eq(1L), any(ClubProfileRequest.class), eq(false)))
        .thenReturn(resp);

    mockMvc
        .perform(
            put("/api/clubs/1/profiles/1")
                .param("sync", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ClubProfileRequest("Updated", List.of("manage_events")))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated"));
  }

  // ── deleteProfile ───────────────────────────────────────────────────

  @Test
  void deleteProfile_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/clubs/1/profiles/1").param("sync", "true"))
        .andExpect(status().isNoContent());
    verify(clubProfileService).deleteProfile(1L, 1L, true);
  }

  // ── listProfiles ────────────────────────────────────────────────────

  @Test
  void listProfiles_returns200() throws Exception {
    ClubProfileResponse p1 = new ClubProfileResponse(1L, "A", List.of("manage_events"));
    ClubProfileResponse p2 = new ClubProfileResponse(2L, "B", List.of("manage_posts"));
    when(clubProfileService.listProfiles(1L)).thenReturn(List.of(p1, p2));

    mockMvc
        .perform(get("/api/clubs/1/profiles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("A"));
  }

  // ── assignPrivileges ────────────────────────────────────────────────

  @Test
  void assignPrivileges_returns201() throws Exception {
    mockMvc
        .perform(
            post("/api/clubs/1/members/50/privileges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AssignPrivilegesRequest(100L, List.of("manage_events")))))
        .andExpect(status().isCreated());
    verify(clubProfileService).assignPrivileges(eq(1L), eq(50L), any(AssignPrivilegesRequest.class));
  }

  // ── unassignProfile ─────────────────────────────────────────────────

  @Test
  void unassignProfile_returns200() throws Exception {
    mockMvc
        .perform(delete("/api/clubs/1/members/50/profiles/100"))
        .andExpect(status().isOk());
    verify(clubProfileService).unassignProfile(1L, 50L, 100L);
  }

  // ── revokePrivilege ─────────────────────────────────────────────────

  @Test
  void revokePrivilege_returns200() throws Exception {
    mockMvc
        .perform(delete("/api/clubs/1/members/50/privileges/manage_events"))
        .andExpect(status().isOk());
    verify(clubProfileService).revokePrivilege(1L, 50L, "manage_events");
  }

  // ── getMemberPrivileges ─────────────────────────────────────────────

  @Test
  void getMemberPrivileges_returns200() throws Exception {
    MemberPrivilegesResponse resp =
        new MemberPrivilegesResponse(
            50L,
            List.of(
                new MemberPrivilege(
                    200L, "manage_events", new Date(), List.of(new SourceProfile(10L, "P")))));
    when(clubProfileService.getMemberPrivileges(1L, 50L)).thenReturn(resp);

    mockMvc
        .perform(get("/api/clubs/1/members/50/privileges"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.membershipId").value(50))
        .andExpect(jsonPath("$.privileges.length()").value(1))
        .andExpect(jsonPath("$.privileges[0].endpointName").value("manage_events"));
  }
}
