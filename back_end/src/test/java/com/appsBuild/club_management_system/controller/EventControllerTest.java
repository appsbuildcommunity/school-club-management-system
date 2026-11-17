package com.appsBuild.club_management_system.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsBuild.club_management_system.dto.common.ClubReference;
import com.appsBuild.club_management_system.dto.common.Pagination;
import com.appsBuild.club_management_system.dto.event.EventPageResponse;
import com.appsBuild.club_management_system.dto.event.EventRequest;
import com.appsBuild.club_management_system.dto.event.EventResponse;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import com.appsBuild.club_management_system.service.ClubAccessService;
import com.appsBuild.club_management_system.service.EventService;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableWebSecurity
class EventControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EventService eventService;
  @MockitoBean(name = "clubAccess") private ClubAccessService clubAccessService;

  private EventResponse eventResponse() {
    return new EventResponse(
        1L,
        "Workshop",
        "Intro to robotics",
        new ClubReference(2L, "Robotics Club", "A club", null),
        VisibilityLevel.PUBLIC,
        LocalDate.of(2026, 11, 10),
        LocalDate.of(2026, 11, 11),
        List.of(),
        List.of(),
        List.of(),
        List.of());
  }

  @Test
  void listEvents_returns200() throws Exception {
    when(eventService.listEvents(1, 20, null, null, null))
        .thenReturn(new EventPageResponse(List.of(eventResponse()), new Pagination(1, 1, 1)));

    mockMvc
        .perform(get("/api/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events.length()").value(1))
        .andExpect(jsonPath("$.events[0].eventId").value(1))
        .andExpect(jsonPath("$.events[0].title").value("Workshop"))
        .andExpect(jsonPath("$.events[0].startDate").value("2026-11-10"))
        .andExpect(jsonPath("$.pagination.currentPage").value(1))
        .andExpect(jsonPath("$.pagination.totalPages").value(1))
        .andExpect(jsonPath("$.pagination.totalItems").value(1));
  }

  @Test
  void listEvents_passesFilters() throws Exception {
    when(eventService.listEvents(2, 5, "Robotics", null, null))
        .thenReturn(new EventPageResponse(List.of(), new Pagination(2, 3, 11)));

    mockMvc
        .perform(
            get("/api/events").param("page", "2").param("limit", "5").param("club_name", "Robotics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pagination.currentPage").value(2));

    verify(eventService).listEvents(2, 5, "Robotics", null, null);
  }

  @Test
  void getEvent_returns200() throws Exception {
    when(eventService.getEvent(1L)).thenReturn(eventResponse());

    mockMvc
        .perform(get("/api/events/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eventId").value(1))
        .andExpect(jsonPath("$.title").value("Workshop"))
        .andExpect(jsonPath("$.club.id").value(2))
        .andExpect(jsonPath("$.club.name").value("Robotics Club"));
  }

  @Test
  void createEvent_returns201() throws Exception {
    when(eventService.createEvent(eq(2L), any(EventRequest.class))).thenReturn(eventResponse());

    String body =
        """
        {
          "title": "Workshop",
          "description": "Intro to robotics",
          "visibility": "PUBLIC",
          "startDate": "2026-11-10",
          "endDate": "2026-11-11",
          "reservations": []
        }
        """;

    mockMvc
        .perform(
            post("/api/clubs/2/events").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.eventId").value(1));

    verify(eventService).createEvent(eq(2L), any(EventRequest.class));
  }

  @Test
  void createEvent_blankTitle_returns400() throws Exception {
    String body =
        """
        {
          "title": " ",
          "visibility": "PUBLIC",
          "startDate": "2026-11-10",
          "endDate": "2026-11-11"
        }
        """;

    mockMvc
        .perform(
            post("/api/clubs/2/events").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    verify(eventService, never()).createEvent(any(), any());
  }

  @Test
  void updateEvent_returns200() throws Exception {
    EventResponse updated =
        new EventResponse(
            1L,
            "Updated",
            "desc",
            new ClubReference(2L, "Robotics Club", "A club", null),
            VisibilityLevel.MEMBERS_ONLY,
            LocalDate.of(2026, 11, 10),
            LocalDate.of(2026, 11, 11),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    when(eventService.updateEvent(eq(2L), eq(1L), any(EventRequest.class))).thenReturn(updated);

    String body =
        """
        {
          "title": "Updated",
          "description": "desc",
          "visibility": "MEMBERS_ONLY",
          "startDate": "2026-11-10",
          "endDate": "2026-11-11"
        }
        """;

    mockMvc
        .perform(
            put("/api/clubs/2/events/1").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated"))
        .andExpect(jsonPath("$.visibility").value("MEMBERS_ONLY"));

    verify(eventService).updateEvent(eq(2L), eq(1L), any(EventRequest.class));
  }

  @Test
  void deleteEvent_returns204() throws Exception {
    mockMvc.perform(delete("/api/clubs/2/events/1")).andExpect(status().isNoContent());

    verify(eventService).deleteEvent(2L, 1L);
  }
}
