package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.dto.endpoint.EndpointResponse;
import com.appsBuild.club_management_system.repository.EndpointRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EndpointService {

  private final EndpointRepository endpointRepository;
  private final ClubAccessService clubAccessService;

  public List<EndpointResponse> getEndpoints() {
    boolean showAll = clubAccessService.isAdmin() || clubAccessService.isCoordinationClubPresident();
    return (showAll ? endpointRepository.findAll() : endpointRepository.findByPrivilegedFalse())
        .stream()
            .map(
                e ->
                    new EndpointResponse(
                        e.getName(), e.getDescription(), e.getCategory(), e.isPrivileged()))
            .toList();
  }
}
