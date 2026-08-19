package com.appsBuild.club_management_system.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsBuild.club_management_system.dto.endpoint.EndpointResponse;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.service.EndpointService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EndpointController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableWebSecurity
class EndpointControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EndpointService endpointService;

  @BeforeEach
  void setUp() {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .claim("sub", "test-user")
        .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt));
  }

  @Test
  void list_returns200() throws Exception {
    EndpointResponse e1 = new EndpointResponse("manage_events", "Manage events", Category.MANAGE_EVENTS, false);
    EndpointResponse e2 = new EndpointResponse("manage_clubs", "Manage clubs", Category.MANAGE_CLUBS, true);
    when(endpointService.list(any(Jwt.class))).thenReturn(List.of(e1, e2));

    mockMvc
        .perform(get("/api/endpoints"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("manage_events"))
        .andExpect(jsonPath("$[1].privileged").value(true));
  }
}
