package com.appsBuild.club_management_system.service.keycloak;

import com.appsBuild.club_management_system.dto.keycloak.KeycloakWebhookEvent;
import com.appsBuild.club_management_system.dto.keycloak.KeycloakWebhookEvent.Details;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.UserRepository;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakWebhookService {

  private static final String REGISTER_EVENT = "REGISTER";

  private final UserRepository userRepository;

  public void handleEvent(KeycloakWebhookEvent event) {
    if (event == null || !REGISTER_EVENT.equals(event.type()) || event.userId() == null) {
      return;
    }
    if (userRepository.findByKeycloakSub(event.userId()).isPresent()) {
      return;
    }

    Details details = event.details();
    User user =
        User.builder()
            .keycloakSub(event.userId())
            .username(details.username())
            .email(details.email())
            .firstName(details.firstName())
            .lastName(details.lastName())
            .createdAt(new Date())
            .build();
    userRepository.save(user);
  }
}
