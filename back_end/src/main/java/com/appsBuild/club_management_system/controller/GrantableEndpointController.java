package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.annotation.Grantable;
import com.appsBuild.club_management_system.dto.grantableendpoint.GrantableEndpointResponse;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.service.GrantableEndpointService;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grantable-endpoints")
@AllArgsConstructor
public class GrantableEndpointController {

  private final GrantableEndpointService grantableEndpointService;

  @GetMapping
  public ResponseEntity<List<GrantableEndpointResponse>> getGrantableEndpoints() {
    return ResponseEntity.ok(grantableEndpointService.getGrantableEndpoints());
  }

  @GetMapping("/privileged")
  @Grantable(
      name = "view_privileged_endpoints",
      description = "View privileged grantable endpoints in the registry",
      category = Category.VIEW_ENDPOINTS,
      privileged = true)
  @PreAuthorize(
      "hasRole('ADMIN') or @clubAccess.hasCoordinationEndpoint('view_privileged_endpoints')")
  public ResponseEntity<List<GrantableEndpointResponse>> getPrivilegedGrantableEndpoints() {
    return ResponseEntity.ok(grantableEndpointService.getPrivilegedGrantableEndpoints());
  }
}
