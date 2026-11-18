package com.appsBuild.club_management_system.security;

import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Builds a {@link ViewerContext} from the current security context. Public endpoints can call
 * this safely: requests without a JWT are reported as anonymous instead of throwing.
 */
@Component
@AllArgsConstructor
public class ViewerContextResolver {

  private static final String ROLE_PREFIX = "ROLE_";
  private static final String ADMIN = "ADMIN";
  private static final String STUDENT = "STUDENT";

  private final UserRepository userRepository;
  private final ClubMembershipRepository clubMembershipRepository;

  public ViewerContext resolve() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
      return ViewerContext.anonymous();
    }

    boolean admin = hasRole(authentication, ADMIN);
    boolean student = hasRole(authentication, STUDENT);
    Long userId =
        userRepository.findByKeycloakSub(jwt.getSubject()).map(User::getUserId).orElse(null);
    Set<Long> clubIds =
        userId == null
            ? Set.of()
            : Set.copyOf(clubMembershipRepository.findClubIdsByUserId(userId));

    return new ViewerContext(true, admin, student, clubIds);
  }

  private boolean hasRole(Authentication authentication, String role) {
    for (GrantedAuthority authority : authentication.getAuthorities()) {
      if ((ROLE_PREFIX + role).equals(authority.getAuthority())) {
        return true;
      }
    }
    return false;
  }
}
