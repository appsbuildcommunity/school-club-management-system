package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.grantableendpoint.GrantableEndpointResponse;
import com.appsBuild.club_management_system.service.GrantableEndpointService;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grantable-endpoints")
@AllArgsConstructor
public class GrantableEndpointController {

  private final GrantableEndpointService grantableEndpointService;

  @GetMapping
  public ResponseEntity<List<GrantableEndpointResponse>> getEndpoints() {
    return ResponseEntity.ok(grantableEndpointService.getEndpoints());
  }
}
