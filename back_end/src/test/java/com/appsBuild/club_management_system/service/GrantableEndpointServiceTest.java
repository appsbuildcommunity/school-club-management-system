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
  void getGrantableEndpoints_returnsOnlyNonPrivileged() {
    when(grantableEndpointRepository.findByPrivilegedFalse()).thenReturn(List.of(nonPrivileged));

    List<GrantableEndpointResponse> result = grantableEndpointService.getGrantableEndpoints();

    assertEquals(1, result.size());
    assertEquals("manage_events", result.get(0).name());
    assertFalse(result.get(0).privileged());
    verify(grantableEndpointRepository).findByPrivilegedFalse();
    verify(grantableEndpointRepository, never()).findByPrivilegedTrue();
  }

  @Test
  void getPrivilegedGrantableEndpoints_returnsOnlyPrivileged() {
    when(grantableEndpointRepository.findByPrivilegedTrue()).thenReturn(List.of(privileged));

    List<GrantableEndpointResponse> result =
        grantableEndpointService.getPrivilegedGrantableEndpoints();

    assertEquals(1, result.size());
    assertEquals("manage_clubs", result.get(0).name());
    assertTrue(result.get(0).privileged());
    verify(grantableEndpointRepository).findByPrivilegedTrue();
    verify(grantableEndpointRepository, never()).findByPrivilegedFalse();
  }
}
