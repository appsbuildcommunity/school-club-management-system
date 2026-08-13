package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.Endpoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, Long> {

  // Finds the registered endpoint by its unique privilege name, if it exists.
  Optional<Endpoint> findByName(String name);
}
