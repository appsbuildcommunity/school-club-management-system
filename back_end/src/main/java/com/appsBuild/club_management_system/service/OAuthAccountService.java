package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.model.entity.OAuthAccount;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.OAuthProvider;
import com.appsBuild.club_management_system.repository.OAuthAccountRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class OAuthAccountService {

  private final OAuthAccountRepository oauthAccountRepository;
  private final UserRepository userRepository;
  private final Set<String> bootstrapAdminEmails;

  public OAuthAccountService(
      OAuthAccountRepository oauthAccountRepository,
      UserRepository userRepository,
      @Value("${app.auth.bootstrap-admin-emails:}") String bootstrapAdminEmails) {
    this.oauthAccountRepository = oauthAccountRepository;
    this.userRepository = userRepository;
    this.bootstrapAdminEmails =
        Arrays.stream(bootstrapAdminEmails.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(email -> email.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Resolves the local user for a provider identity, creating one on first sign-in.
   *
   * <p>An existing local email is never auto-linked to a new provider. That prevents an unverified
   * provider email (Facebook) from taking over an account created through a verified provider
   * (Google). Account linking will be an explicit authenticated action.
   */
  @Transactional
  public User signIn(
      OAuthProvider provider,
      String providerSubject,
      String providerEmail,
      boolean emailVerified,
      String displayName) {
    if (!StringUtils.hasText(providerSubject)) {
      throw new ApplicationException("OAuth provider did not return a subject");
    }
    if (!StringUtils.hasText(providerEmail)) {
      throw new ApplicationException(
          "OAuth provider did not return an email address; grant email access and try again");
    }

    String email = providerEmail.trim().toLowerCase(Locale.ROOT);
    if (email.length() > 100) {
      throw new ApplicationException("OAuth provider email is too long");
    }

    Optional<OAuthAccount> existing =
        oauthAccountRepository.findByProviderAndProviderSubject(provider, providerSubject);
    if (existing.isPresent()) {
      OAuthAccount account = existing.get();
      if (!email.equalsIgnoreCase(account.getProviderEmail())) {
        account.setProviderEmail(email);
        account.setEmailVerified(emailVerified);
      }
      return account.getUser();
    }

    if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
      throw new ConflictException(
          "An account with this email already exists. Sign in with the provider you used to create it.");
    }

    User user = createUser(email, displayName);
    OAuthAccount account =
        OAuthAccount.builder()
            .user(user)
            .provider(provider)
            .providerSubject(providerSubject)
            .providerEmail(email)
            .emailVerified(emailVerified)
            .createdAt(new Date())
            .build();
    oauthAccountRepository.save(account);
    log.info("Provisioned user {} via {}", user.getUserId(), provider);
    return user;
  }

  private User createUser(String email, String displayName) {
    String[] names = splitName(displayName, email);
    User user =
        User.builder()
            .firstName(names[0])
            .lastName(names[1])
            .username(uniqueUsername(email))
            .email(email)
            .role(bootstrapAdminEmails.contains(email) ? "ADMIN" : "USER")
            .createdAt(new Date())
            .build();
    return userRepository.save(user);
  }

  private String uniqueUsername(String email) {
    int at = email.indexOf('@');
    String base = at > 0 ? email.substring(0, at) : email;
    base = base.replaceAll("[^A-Za-z0-9._-]", "-");
    if (!StringUtils.hasText(base)) {
      base = "user";
    }
    if (base.length() > 44) {
      base = base.substring(0, 44);
    }
    String candidate = base;
    int suffix = 2;
    while (userRepository.findByUsername(candidate).isPresent()) {
      String suffixText = "-" + suffix++;
      int maxBaseLength = 50 - suffixText.length();
      candidate = base.substring(0, Math.min(base.length(), maxBaseLength)) + suffixText;
    }
    return candidate;
  }

  private String[] splitName(String displayName, String email) {
    String name = StringUtils.hasText(displayName) ? displayName.trim() : localPart(email);
    int space = name.indexOf(' ');
    String firstName = space > 0 ? name.substring(0, space) : name;
    String lastName = space > 0 ? name.substring(space + 1).trim() : "";
    if (!StringUtils.hasText(firstName)) {
      firstName = "User";
    }
    return new String[] {truncate(firstName, 50), truncate(lastName, 50)};
  }

  private String localPart(String email) {
    int at = email.indexOf('@');
    return at > 0 ? email.substring(0, at) : email;
  }

  private String truncate(String value, int maxLength) {
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
