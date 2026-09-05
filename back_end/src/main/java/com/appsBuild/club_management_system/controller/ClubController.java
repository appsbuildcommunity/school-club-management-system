package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.annotation.GrantableEndpoint;
import com.appsBuild.club_management_system.dto.club.ClubDetailResponse;
import com.appsBuild.club_management_system.dto.club.ClubRequest;
import com.appsBuild.club_management_system.dto.club.ClubResponse;
import com.appsBuild.club_management_system.dto.club.PresidentRequest;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.service.ClubService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs")
@AllArgsConstructor
public class ClubController {

  private final ClubService clubService;

  // ── Create ──────────────────────────────────────────────────────────

  @PostMapping("/coordination")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ClubResponse> createCoordinationClub(
      @Valid @RequestBody ClubRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(clubService.createCoordinationClub(request));
  }

  @PostMapping
  @GrantableEndpoint(
      name = "create_club",
      description = "Create a new club",
      category = Category.MANAGE_CLUBS,
      privileged = true)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasCoordinationEndpoint('create_club')")
  public ResponseEntity<ClubResponse> createClub(@Valid @RequestBody ClubRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createClub(request));
  }

  // ── Read ────────────────────────────────────────────────────────────

  @GetMapping
  public ResponseEntity<List<ClubResponse>> listClubs() {
    return ResponseEntity.ok(clubService.listClubs());
  }

  @GetMapping("/{clubName}")
  public ResponseEntity<ClubDetailResponse> getClubDetails(@PathVariable String clubName) {
    return ResponseEntity.ok(clubService.getClubDetails(clubName));
  }

  // ── Update / Delete ─────────────────────────────────────────────────

  @PutMapping("/{clubId}")
  @GrantableEndpoint(
      name = "update_club",
      description = "Update a club's information",
      category = Category.MANAGE_CLUBS,
      privileged = true)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'update_club')")
  public ResponseEntity<ClubResponse> updateClub(
      @PathVariable Long clubId, @Valid @RequestBody ClubRequest request) {
    return ResponseEntity.ok(clubService.updateClub(clubId, request));
  }

  @DeleteMapping("/{clubId}")
  @GrantableEndpoint(
      name = "delete_club",
      description = "Delete a club",
      category = Category.MANAGE_CLUBS,
      privileged = true)
  @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'delete_club')")
  public ResponseEntity<Void> deleteClub(@PathVariable Long clubId) {
    clubService.deleteClub(clubId);
    return ResponseEntity.noContent().build();
  }

  // ── President management ────────────────────────────────────────────

  @PutMapping("/{clubId}/president")
  @GrantableEndpoint(
      name = "change_club_president",
      description = "Change the president of a club",
      category = Category.MANAGE_CLUBS,
      privileged = true)
  @PreAuthorize(
      "hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'change_club_president')")
  public ResponseEntity<Void> changePresident(
      @PathVariable Long clubId, @Valid @RequestBody PresidentRequest request) {
    clubService.changePresident(clubId, request);
    return ResponseEntity.ok().build();
  }
}
