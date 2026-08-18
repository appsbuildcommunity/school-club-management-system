package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.dto.endpoint.EndpointResponse;
import com.appsBuild.club_management_system.repository.EndpointRepository;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EndpointService {

  private final EndpointRepository endpointRepository;
  private final ClubAccessService clubAccessService;

  public List<EndpointResponse> list(Jwt jwt) {
    boolean showAll = clubAccessService.isAdmin(jwt) || clubAccessService.isCoordinationClubPresident(jwt);
    return (showAll ? endpointRepository.findAll() : endpointRepository.findByPrivilegedFalse())
        .stream()
        .map(e -> new EndpointResponse(e.getName(), e.getDescription(), e.getCategory(), e.isPrivileged()))
        .toList();
  }
}
