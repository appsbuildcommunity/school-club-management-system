package com.appsBuild.club_management_system.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsBuild.club_management_system.dto.club.ClubDetailResponse;
import com.appsBuild.club_management_system.dto.club.ClubRequest;
import com.appsBuild.club_management_system.dto.club.ClubResponse;
import com.appsBuild.club_management_system.dto.club.MemberResponse;
import com.appsBuild.club_management_system.dto.club.PresidentRequest;
import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.service.ClubAccessService;
import com.appsBuild.club_management_system.service.ClubService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClubController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableWebSecurity
class ClubControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private ClubService clubService;
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

  // ── createCoordinationClub ──────────────────────────────────────────

  @Test
  void createCoordinationClub_returns201() throws Exception {
    ClubResponse resp =
        new ClubResponse(1L, "Coordination", "School Coordination", "hub", true, null, 0L);
    when(clubService.createCoordinationClub(any(ClubRequest.class))).thenReturn(resp);

    mockMvc
        .perform(
            post("/api/clubs/coordination")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ClubRequest("Coordination", "School Coordination", "hub", null))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.clubId").value(1))
        .andExpect(jsonPath("$.isCoordinationClub").value(true));
  }

  @Test
  void createCoordinationClub_blankName_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/clubs/coordination")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ClubRequest("", null, null, null))))
        .andExpect(status().isBadRequest());
    verify(clubService, never()).createCoordinationClub(any());
  }

  // ── createClub ──────────────────────────────────────────────────────

  @Test
  void createClub_returns201() throws Exception {
    ClubResponse resp =
        new ClubResponse(2L, "Chess Club", "Chess Lovers", null, false, null, 0L);
    when(clubService.createClub(any(ClubRequest.class))).thenReturn(resp);

    mockMvc
        .perform(
            post("/api/clubs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ClubRequest("Chess Club", "Chess Lovers", null, "alice"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.clubName").value("Chess Club"))
        .andExpect(jsonPath("$.memberCount").value(0));
  }

  // ── listClubs ───────────────────────────────────────────────────────

  @Test
  void listClubs_returns200() throws Exception {
    when(clubService.listClubs())
        .thenReturn(
            List.of(
                new ClubResponse(1L, "A", null, null, true, null, 3L),
                new ClubResponse(2L, "B", null, null, false, null, 25L)));

    mockMvc
        .perform(get("/api/clubs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[1].memberCount").value(25));
  }

  // ── getClubDetails ──────────────────────────────────────────────────

  @Test
  void getClubDetails_returns200WithStaff() throws Exception {
    MemberResponse president =
        new MemberResponse("alice", "Alice", "Smith", null, ClubRole.CLUB_PRESIDENT);
    MemberResponse assistant =
        new MemberResponse("bob", "Bob", "Jones", null, ClubRole.ASSISTANT_MEMBER);
    when(clubService.getClubDetails("Chess Club"))
        .thenReturn(new ClubDetailResponse(2L, "Chess Club", null, null, false, null, 12L,
            List.of(president, assistant)));

    mockMvc
        .perform(get("/api/clubs/Chess Club"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberCount").value(12))
        .andExpect(jsonPath("$.staff.length()").value(2))
        .andExpect(jsonPath("$.staff[0].username").value("alice"))
        .andExpect(jsonPath("$.staff[0].role").value("CLUB_PRESIDENT"));
  }

  // ── updateClub ──────────────────────────────────────────────────────

  @Test
  void updateClub_returns200() throws Exception {
    ClubResponse resp = new ClubResponse(2L, "Renamed", null, null, false, null, 5L);
    when(clubService.updateClub(eq(2L), any(ClubRequest.class))).thenReturn(resp);

    mockMvc
        .perform(
            put("/api/clubs/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ClubRequest("Renamed", null, null, null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clubName").value("Renamed"));
  }

  @Test
  void deleteClub_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/clubs/2"))
        .andExpect(status().isNoContent());
    verify(clubService).deleteClub(2L);
  }

  @Test
  void deleteCoordinationClub_mapsConflictTo409() throws Exception {
    doThrow(new ConflictException("The coordination club cannot be deleted"))
        .when(clubService).deleteClub(1L);

    mockMvc
        .perform(delete("/api/clubs/1"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONFLICT"));
  }

  // ── changePresident ─────────────────────────────────────────────────

  @Test
  void changePresident_returns200() throws Exception {
    mockMvc
        .perform(
            put("/api/clubs/2/president")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PresidentRequest("bob"))))
        .andExpect(status().isOk());
    verify(clubService).changePresident(eq(2L), any(PresidentRequest.class));
  }

  @Test
  void changePresident_blankUsername_returns400() throws Exception {
    mockMvc
        .perform(
            put("/api/clubs/2/president")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"presidentUsername\":\"\"}"))
        .andExpect(status().isBadRequest());
    verify(clubService, never()).changePresident(any(), any());
  }
}
