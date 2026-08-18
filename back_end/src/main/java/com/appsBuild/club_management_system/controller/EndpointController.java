package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.endpoint.EndpointResponse;
import com.appsBuild.club_management_system.service.EndpointService;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/endpoints")
@AllArgsConstructor
public class EndpointController {

  private final EndpointService endpointService;

  @GetMapping
  public ResponseEntity<List<EndpointResponse>> list(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(endpointService.list(jwt));
  }
}
