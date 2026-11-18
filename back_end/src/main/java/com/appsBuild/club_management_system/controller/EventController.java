package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.annotation.Grantable;
import com.appsBuild.club_management_system.dto.event.EventPageResponse;
import com.appsBuild.club_management_system.dto.event.EventRequest;
import com.appsBuild.club_management_system.dto.event.EventResponse;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.service.EventService;

import jakarta.validation.Valid;

import java.time.LocalDate;

import lombok.AllArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class EventController {

  private final EventService eventService;

  // ── Read ────────────────────────────────────────────────────────────

  @GetMapping("/events")
  public ResponseEntity<EventPageResponse> listEvents(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(name = "club_name", required = false) String clubName,
      @RequestParam(name = "start_date", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(name = "end_date", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    return ResponseEntity.ok(eventService.listEvents(page, limit, clubName, startDate, endDate));
  }

  @GetMapping("/events/{eventId}")
  public ResponseEntity<EventResponse> getEvent(@PathVariable Long eventId) {
    return ResponseEntity.ok(eventService.getEvent(eventId));
  }

  // ── Write ───────────────────────────────────────────────────────────

  @PostMapping("/clubs/{clubId}/events")
  @Grantable(
      name = "manage_events",
      description = "Manage events for a club",
      category = Category.MANAGE_EVENTS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'manage_events')")
  public ResponseEntity<EventResponse> createEvent(
      @PathVariable Long clubId, @Valid @RequestBody EventRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(eventService.createEvent(clubId, request));
  }

  @PutMapping("/clubs/{clubId}/events/{eventId}")
  @Grantable(
      name = "manage_events",
      description = "Manage events for a club",
      category = Category.MANAGE_EVENTS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'manage_events')")
  public ResponseEntity<EventResponse> updateEvent(
      @PathVariable Long clubId,
      @PathVariable Long eventId,
      @Valid @RequestBody EventRequest request) {
    return ResponseEntity.ok(eventService.updateEvent(clubId, eventId, request));
  }

  @DeleteMapping("/clubs/{clubId}/events/{eventId}")
  @Grantable(
      name = "manage_events",
      description = "Manage events for a club",
      category = Category.MANAGE_EVENTS,
      privileged = false)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'manage_events')")
  public ResponseEntity<Void> deleteEvent(
      @PathVariable Long clubId, @PathVariable Long eventId) {
    eventService.deleteEvent(clubId, eventId);
    return ResponseEntity.noContent().build();
  }
}
