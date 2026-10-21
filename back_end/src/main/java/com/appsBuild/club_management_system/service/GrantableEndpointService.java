package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.dto.grantableendpoint.GrantableEndpointResponse;
import com.appsBuild.club_management_system.repository.GrantableEndpointRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GrantableEndpointService {

  private final GrantableEndpointRepository grantableEndpointRepository;

  public List<GrantableEndpointResponse> getGrantableEndpoints() {
    return grantableEndpointRepository.findByPrivilegedFalse().stream()
        .map(
            e ->
                new GrantableEndpointResponse(
                    e.getName(), e.getDescription(), e.getCategory(), e.isPrivileged()))
        .toList();
  }

  public List<GrantableEndpointResponse> getPrivilegedGrantableEndpoints() {
    return grantableEndpointRepository.findByPrivilegedTrue().stream()
        .map(
            e ->
                new GrantableEndpointResponse(
                    e.getName(), e.getDescription(), e.getCategory(), e.isPrivileged()))
        .toList();
  }
}
