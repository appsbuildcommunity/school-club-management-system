package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.OAuthAccount;
import com.appsBuild.club_management_system.model.enums.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
  // The linked user must stay initialized after the sign-in transaction closes so a JWT can be
  // issued from it.
  @EntityGraph(attributePaths = "user")
  Optional<OAuthAccount> findByProviderAndProviderSubject(
      OAuthProvider provider, String providerSubject);
}
