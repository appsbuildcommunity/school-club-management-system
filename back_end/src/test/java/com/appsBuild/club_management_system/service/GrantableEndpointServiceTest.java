package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.appsBuild.club_management_system.dto.grantableendpoint.GrantableEndpointResponse;
import com.appsBuild.club_management_system.model.entity.GrantableEndpoint;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.repository.GrantableEndpointRepository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrantableEndpointServiceTest {

  @Mock private GrantableEndpointRepository grantableEndpointRepository;
  @Mock private ClubAccessService clubAccessService;

  @InjectMocks private GrantableEndpointService grantableEndpointService;

  private GrantableEndpoint nonPrivileged;
  private GrantableEndpoint privileged;

  @BeforeEach
  void setUp() {
    nonPrivileged =
        GrantableEndpoint.builder()
            .endpointId(1L)
            .name("manage_events")
            .description("Manage events")
            .category(Category.MANAGE_EVENTS)
            .privileged(false)
            .build();
    privileged =
        GrantableEndpoint.builder()
            .endpointId(2L)
            .name("manage_clubs")
            .description("Manage clubs")
            .category(Category.MANAGE_CLUBS)
            .privileged(true)
            .build();
  }

  @Test
  void getEndpoints_adminSeesAll() {
    when(clubAccessService.isAdmin()).thenReturn(true);
    when(grantableEndpointRepository.findAll()).thenReturn(List.of(nonPrivileged, privileged));

    List<GrantableEndpointResponse> result = grantableEndpointService.getEndpoints();

    assertEquals(2, result.size());
    verify(grantableEndpointRepository).findAll();
    verify(grantableEndpointRepository, never()).findByPrivilegedFalse();
  }

  @Test
  void getEndpoints_coordinationPresidentSeesAll() {
    when(clubAccessService.isAdmin()).thenReturn(false);
    when(clubAccessService.isCoordinationClubPresident()).thenReturn(true);
    when(grantableEndpointRepository.findAll()).thenReturn(List.of(nonPrivileged, privileged));

    List<GrantableEndpointResponse> result = grantableEndpointService.getEndpoints();

    assertEquals(2, result.size());
    verify(grantableEndpointRepository).findAll();
  }

  @Test
  void getEndpoints_regularMemberSeesOnlyNonPrivileged() {
    when(clubAccessService.isAdmin()).thenReturn(false);
    when(clubAccessService.isCoordinationClubPresident()).thenReturn(false);
    when(grantableEndpointRepository.findByPrivilegedFalse()).thenReturn(List.of(nonPrivileged));

    List<GrantableEndpointResponse> result = grantableEndpointService.getEndpoints();

    assertEquals(1, result.size());
    assertEquals("manage_events", result.get(0).name());
    assertFalse(result.get(0).privileged());
    verify(grantableEndpointRepository, never()).findAll();
  }
}
