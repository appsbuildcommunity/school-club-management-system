package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.GrantableEndpoint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrantableEndpointRepository extends JpaRepository<GrantableEndpoint, Long> {

  // Finds the registered endpoint by its unique privilege name, if it exists.
  Optional<GrantableEndpoint> findByName(String name);

  // Lists all non-privileged endpoints (used by the open registry view).
  List<GrantableEndpoint> findByPrivilegedFalse();

  // Lists all privileged endpoints (used by the privileged registry view).
  List<GrantableEndpoint> findByPrivilegedTrue();
}
