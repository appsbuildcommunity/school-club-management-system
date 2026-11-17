package com.appsBuild.club_management_system.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsBuild.club_management_system.dto.grantableendpoint.GrantableEndpointResponse;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.service.GrantableEndpointService;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GrantableEndpointController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableWebSecurity
class GrantableEndpointControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GrantableEndpointService grantableEndpointService;

  @Test
  void getGrantableEndpoints_returns200() throws Exception {
    GrantableEndpointResponse e1 =
        new GrantableEndpointResponse("manage_events", "Manage events", Category.MANAGE_EVENTS, false);
    when(grantableEndpointService.getGrantableEndpoints()).thenReturn(List.of(e1));

    mockMvc
        .perform(get("/api/grantable-endpoints"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("manage_events"))
        .andExpect(jsonPath("$[0].privileged").value(false));
  }

  @Test
  void getPrivilegedGrantableEndpoints_returns200() throws Exception {
    GrantableEndpointResponse e2 =
        new GrantableEndpointResponse("manage_clubs", "Manage clubs", Category.MANAGE_CLUBS, true);
    when(grantableEndpointService.getPrivilegedGrantableEndpoints()).thenReturn(List.of(e2));

    mockMvc
        .perform(get("/api/grantable-endpoints/privileged"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("manage_clubs"))
        .andExpect(jsonPath("$[0].privileged").value(true));
  }
}
