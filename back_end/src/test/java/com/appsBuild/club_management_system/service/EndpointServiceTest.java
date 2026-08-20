package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.appsBuild.club_management_system.dto.endpoint.EndpointResponse;
import com.appsBuild.club_management_system.model.entity.Endpoint;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.repository.EndpointRepository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class EndpointServiceTest {

  @Mock private EndpointRepository endpointRepository;
  @Mock private ClubAccessService clubAccessService;

  @InjectMocks private EndpointService endpointService;

  private Endpoint nonPrivileged;
  private Endpoint privileged;

  @BeforeEach
  void setUp() {
    nonPrivileged =
        Endpoint.builder()
            .endpointId(1L)
            .name("manage_events")
            .description("Manage events")
            .category(Category.MANAGE_EVENTS)
            .privileged(false)
            .build();
    privileged =
        Endpoint.builder()
            .endpointId(2L)
            .name("manage_clubs")
            .description("Manage clubs")
            .category(Category.MANAGE_CLUBS)
            .privileged(true)
            .build();
  }

  @Test
  void list_adminSeesAll() {
    Jwt jwt = mock(Jwt.class);
    when(clubAccessService.isAdmin(jwt)).thenReturn(true);
    when(endpointRepository.findAll()).thenReturn(List.of(nonPrivileged, privileged));

    List<EndpointResponse> result = endpointService.list(jwt);

    assertEquals(2, result.size());
    verify(endpointRepository).findAll();
    verify(endpointRepository, never()).findByPrivilegedFalse();
  }

  @Test
  void list_coordinationPresidentSeesAll() {
    Jwt jwt = mock(Jwt.class);
    when(clubAccessService.isAdmin(jwt)).thenReturn(false);
    when(clubAccessService.isCoordinationClubPresident(jwt)).thenReturn(true);
    when(endpointRepository.findAll()).thenReturn(List.of(nonPrivileged, privileged));

    List<EndpointResponse> result = endpointService.list(jwt);

    assertEquals(2, result.size());
    verify(endpointRepository).findAll();
  }

  @Test
  void list_regularMemberSeesOnlyNonPrivileged() {
    Jwt jwt = mock(Jwt.class);
    when(clubAccessService.isAdmin(jwt)).thenReturn(false);
    when(clubAccessService.isCoordinationClubPresident(jwt)).thenReturn(false);
    when(endpointRepository.findByPrivilegedFalse()).thenReturn(List.of(nonPrivileged));

    List<EndpointResponse> result = endpointService.list(jwt);

    assertEquals(1, result.size());
    assertEquals("manage_events", result.get(0).name());
    assertFalse(result.get(0).privileged());
    verify(endpointRepository, never()).findAll();
  }
}
