package com.appsBuild.club_management_system.security;

import java.util.Set;

/**
 * Snapshot of the caller used for read-side visibility decisions.
 *
 * <p>{@code authenticated} is true whenever the request carries a valid JWT, even if the user
 * has not finished onboarding yet. {@code admin} and {@code student} come from the realm roles in
 * the token, while {@code clubIds} lists the clubs the user belongs to (used for
 * members-only content).
 */
public record ViewerContext(
    boolean authenticated, boolean admin, boolean student, Set<Long> clubIds) {

  public ViewerContext {
    clubIds = clubIds == null ? Set.of() : Set.copyOf(clubIds);
  }

  public static ViewerContext anonymous() {
    return new ViewerContext(false, false, false, Set.of());
  }
}
