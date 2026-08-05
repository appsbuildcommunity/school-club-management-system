package com.appsBuild.club_management_system.service.keycloak;

import com.appsBuild.club_management_system.exception.ApplicationException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class KeycloakAdminService {

  private static final String GRANT_TYPE = "client_credentials";
  private static final String USER_ROLE = "USER";
  private static final String STUDENT_ROLE = "STUDENT";

  private final RestClient restClient;

  @Value("${keycloak.url}")
  private String keycloakUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.admin.client-id}")
  private String adminClientId;

  @Value("${keycloak.admin.client-secret}")
  private String adminClientSecret;

  public KeycloakAdminService(RestClient.Builder builder) {
    this.restClient = builder.build();
  }

  public void updateRealmRole(String keycloakSub, String targetRole) {
    String token = getAccessToken();
    List<Map<String, Object>> currentRoles = getRealmRoles(token, keycloakSub);

    boolean alreadyTarget =
        currentRoles.stream().anyMatch(role -> targetRole.equals(role.get("name")));
    if (alreadyTarget) {
      return;
    }

    List<Map<String, Object>> rolesToRemove =
        currentRoles.stream()
            .filter(
                role -> {
                  Object name = role.get("name");
                  return USER_ROLE.equals(name) || STUDENT_ROLE.equals(name);
                })
            .toList();
    if (!rolesToRemove.isEmpty()) {
      removeRealmRoles(token, keycloakSub, rolesToRemove);
    }

    Map<String, Object> targetRoleRepresentation = getRole(token, targetRole);
    if (targetRoleRepresentation == null) {
      throw new ApplicationException("Keycloak role not found: " + targetRole);
    }
    addRealmRole(token, keycloakSub, targetRoleRepresentation);
  }

  private String getAccessToken() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", GRANT_TYPE);
    form.add("client_id", adminClientId);
    form.add("client_secret", adminClientSecret);

    Map<String, Object> response =
        postForJson(
            tokenEndpoint(),
            form,
            MediaType.APPLICATION_FORM_URLENCODED);
    String accessToken = (String) response.get("access_token");
    if (accessToken == null || accessToken.isBlank()) {
      throw new ApplicationException("Failed to obtain a Keycloak admin access token");
    }
    return accessToken;
  }

  private List<Map<String, Object>> getRealmRoles(String token, String keycloakSub) {
    try {
      List<Map<String, Object>> roles =
          restClient
              .get()
              .uri(roleMappingsEndpoint(keycloakSub))
              .header("Authorization", bearer(token))
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return roles == null ? List.of() : roles;
    } catch (RestClientResponseException e) {
      throw new ApplicationException(
          "Failed to read Keycloak roles for user " + keycloakSub + ": " + e.getStatusCode());
    }
  }

  private Map<String, Object> getRole(String token, String roleName) {
    try {
      Map<String, Object> response =
          restClient
              .get()
              .uri(roleEndpoint(roleName))
              .header("Authorization", bearer(token))
              .retrieve()
              .body(new ParameterizedTypeReference<Map<String, Object>>() {});
      if (response == null) {
        throw new ApplicationException("Keycloak role not found: " + roleName);
      }
      return response;
    } catch (RestClientResponseException e) {
      throw new ApplicationException(
          "Failed to read Keycloak role " + roleName + ": " + e.getStatusCode());
    }
  }

  private void removeRealmRoles(String token, String keycloakSub, List<Map<String, Object>> roles) {
    try {
      restClient
          .method(org.springframework.http.HttpMethod.DELETE)
          .uri(roleMappingsEndpoint(keycloakSub))
          .header("Authorization", bearer(token))
          .contentType(MediaType.APPLICATION_JSON)
          .body(roles)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientResponseException e) {
      throw new ApplicationException(
          "Failed to remove Keycloak roles for user " + keycloakSub + ": " + e.getStatusCode());
    }
  }

  private void addRealmRole(String token, String keycloakSub, Map<String, Object> role) {
    try {
      restClient
          .post()
          .uri(roleMappingsEndpoint(keycloakSub))
          .header("Authorization", bearer(token))
          .contentType(MediaType.APPLICATION_JSON)
          .body(List.of(role))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientResponseException e) {
      throw new ApplicationException(
          "Failed to add Keycloak role for user " + keycloakSub + ": " + e.getStatusCode());
    }
  }

  private Map<String, Object> postForJson(String uri, Object body, MediaType contentType) {
    try {
      Map<String, Object> response =
          restClient
              .post()
              .uri(uri)
              .contentType(contentType)
              .body(body)
              .retrieve()
              .body(new ParameterizedTypeReference<Map<String, Object>>() {});
      if (response == null) {
        throw new ApplicationException("Empty response from " + uri);
      }
      return response;
    } catch (RestClientResponseException e) {
      throw new ApplicationException("Keycloak request to " + uri + " failed: " + e.getStatusCode());
    }
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }

  private String tokenEndpoint() {
    return keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
  }

  private String roleMappingsEndpoint(String keycloakSub) {
    return keycloakUrl + "/admin/realms/" + realm + "/users/" + keycloakSub + "/role-mappings/realm";
  }

  private String roleEndpoint(String roleName) {
    return keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;
  }
}
