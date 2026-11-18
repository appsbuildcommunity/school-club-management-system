package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.model.entity.OAuthAccount;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.OAuthProvider;
import com.appsBuild.club_management_system.repository.OAuthAccountRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthAccountServiceTest {

  @Mock private OAuthAccountRepository oauthAccountRepository;
  @Mock private UserRepository userRepository;

  private OAuthAccountService service;

  @BeforeEach
  void setUp() {
    service = new OAuthAccountService(oauthAccountRepository, userRepository, "");
  }

  private static String expectedHashedUsername(String email) {
    String base = email.substring(0, email.indexOf('@'));
    String hash =
        UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8))
            .toString()
            .replace("-", "")
            .substring(0, 12);
    return base + "-" + hash;
  }

  private void stubSaves() {
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setUserId(7L);
              return saved;
            });
    when(oauthAccountRepository.save(any(OAuthAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void signIn_newGoogleUser_provisionsLocalUserAndAccount() {
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setUserId(7L);
              return saved;
            });
    when(oauthAccountRepository.save(any(OAuthAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    User result =
        service.signIn(OAuthProvider.GOOGLE, "google-sub", "Alice@Example.com", true, "Alice Doe");

    assertEquals(7L, result.getUserId());
    assertEquals("alice", result.getUsername());
    assertEquals("alice@example.com", result.getEmail());
    assertEquals("Alice", result.getFirstName());
    assertEquals("Doe", result.getLastName());
    assertEquals("USER", result.getRole());
    assertFalse(result.isOnboardingCompleted());

    ArgumentCaptor<OAuthAccount> accountCaptor = ArgumentCaptor.forClass(OAuthAccount.class);
    verify(oauthAccountRepository).save(accountCaptor.capture());
    OAuthAccount account = accountCaptor.getValue();
    assertEquals(OAuthProvider.GOOGLE, account.getProvider());
    assertEquals("google-sub", account.getProviderSubject());
    assertEquals("alice@example.com", account.getProviderEmail());
    assertTrue(account.isEmailVerified());
    assertSame(result, account.getUser());
    verify(userRepository, times(1)).findByUsername(anyString());
  }

  @Test
  void signIn_existingProviderAccount_returnsLinkedUser() {
    User existing = User.builder().userId(3L).role("ADMIN").build();
    OAuthAccount account =
        OAuthAccount.builder()
            .oauthAccountId(9L)
            .user(existing)
            .provider(OAuthProvider.GOOGLE)
            .providerSubject("google-sub")
            .providerEmail("alice@example.com")
            .build();
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.of(account));

    User result =
        service.signIn(OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe");

    assertSame(existing, result);
    verify(userRepository, never()).findByEmailIgnoreCase(any());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void signIn_existingProviderAccount_updatesChangedProviderEmail() {
    User existing = User.builder().userId(3L).role("USER").build();
    OAuthAccount account =
        OAuthAccount.builder()
            .user(existing)
            .provider(OAuthProvider.GOOGLE)
            .providerSubject("google-sub")
            .providerEmail("old@example.com")
            .emailVerified(false)
            .build();
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.of(account));

    service.signIn(OAuthProvider.GOOGLE, "google-sub", "New@Example.com", true, "Alice Doe");

    assertEquals("new@example.com", account.getProviderEmail());
    assertTrue(account.isEmailVerified());
  }

  @Test
  void signIn_existingEmail_throwsConflict() {
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.FACEBOOK, "fb-id"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice@example.com"))
        .thenReturn(Optional.of(User.builder().userId(1L).build()));

    ConflictException exception =
        assertThrows(
            ConflictException.class,
            () ->
                service.signIn(
                    OAuthProvider.FACEBOOK, "fb-id", "alice@example.com", false, "Alice Doe"));

    assertTrue(exception.getMessage().contains("already exists"));
    verify(userRepository, never()).save(any(User.class));
    verify(oauthAccountRepository, never()).save(any(OAuthAccount.class));
  }

  @Test
  void signIn_bootstrapAdminEmail_getsAdminRole() {
    service = new OAuthAccountService(oauthAccountRepository, userRepository, " Admin@Example.com ");
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "admin-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setUserId(8L);
              return saved;
            });
    when(oauthAccountRepository.save(any(OAuthAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    User result =
        service.signIn(OAuthProvider.GOOGLE, "admin-sub", "admin@example.com", true, "Admin User");

    assertEquals("ADMIN", result.getRole());
  }

  @Test
  void signIn_usernameCollision_usesEmailHashSuffix() {
    String hashed = expectedHashedUsername("alice@example.com");
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(User.builder().userId(1L).build()));
    when(userRepository.findByUsername(hashed)).thenReturn(Optional.empty());
    stubSaves();

    User result =
        service.signIn(OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe");

    assertEquals(hashed, result.getUsername());
    verify(userRepository).findByUsername("alice");
    verify(userRepository).findByUsername(hashed);
    verify(userRepository, times(2)).findByUsername(anyString());
  }

  @Test
  void signIn_hashedUsernameAlsoTaken_fallsBackToRandomSuffixWithBoundedLookups() {
    String hashed = expectedHashedUsername("alice@example.com");
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(User.builder().userId(1L).build()));
    when(userRepository.findByUsername(hashed))
        .thenReturn(Optional.of(User.builder().userId(2L).build()));
    when(userRepository.findByUsername(
            argThat(
                name ->
                    name != null
                        && name.startsWith("alice-")
                        && !name.equals(hashed))))
        .thenReturn(Optional.empty());
    stubSaves();

    User result =
        service.signIn(OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe");

    assertTrue(result.getUsername().startsWith("alice-"));
    assertNotEquals(hashed, result.getUsername());
    assertTrue(result.getUsername().length() <= 50);
    verify(userRepository, times(3)).findByUsername(anyString());
  }

  @Test
  void signIn_allUsernameCandidatesTaken_throwsConflictAfterBoundedLookups() {
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername(anyString()))
        .thenReturn(Optional.of(User.builder().userId(1L).build()));

    ConflictException exception =
        assertThrows(
            ConflictException.class,
            () ->
                service.signIn(
                    OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe"));

    assertTrue(exception.getMessage().contains("unique username"));
    verify(userRepository, times(5)).findByUsername(anyString());
    verify(userRepository, never()).save(any(User.class));
    verify(oauthAccountRepository, never()).save(any(OAuthAccount.class));
  }

  @Test
  void signIn_longEmailLocalPart_keepsUsernameWithinLimit() {
    String email = "a".repeat(60) + "@example.com";
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
    when(userRepository.findByUsername("a".repeat(50))).thenReturn(Optional.empty());
    stubSaves();

    User result = service.signIn(OAuthProvider.GOOGLE, "google-sub", email, true, "Alice Doe");

    assertEquals(50, result.getUsername().length());
    verify(userRepository).findByUsername("a".repeat(50));
  }

  @Test
  void signIn_emailLocalPartWithInvalidCharacters_sanitizesUsername() {
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice+tag@example.com"))
        .thenReturn(Optional.empty());
    when(userRepository.findByUsername("alice-tag")).thenReturn(Optional.empty());
    stubSaves();

    User result =
        service.signIn(
            OAuthProvider.GOOGLE, "google-sub", "alice+tag@example.com", true, "Alice Doe");

    assertEquals("alice-tag", result.getUsername());
  }

  @Test
  void signIn_missingEmail_throwsApplicationException() {
    ApplicationException exception =
        assertThrows(
            ApplicationException.class,
            () -> service.signIn(OAuthProvider.GOOGLE, "google-sub", "  ", true, "Alice Doe"));

    assertTrue(exception.getMessage().contains("email"));
    verifyNoInteractions(oauthAccountRepository, userRepository);
  }

  @Test
  void signIn_missingSubject_throwsApplicationException() {
    ApplicationException exception =
        assertThrows(
            ApplicationException.class,
            () ->
                service.signIn(
                    OAuthProvider.GOOGLE, null, "alice@example.com", true, "Alice Doe"));

    assertTrue(exception.getMessage().contains("subject"));
    verifyNoInteractions(oauthAccountRepository, userRepository);
  }

  @Test
  void signIn_newUserWithoutDisplayName_fallsBackToEmailLocalPart() {
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setUserId(7L);
              return saved;
            });
    when(oauthAccountRepository.save(any(OAuthAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    User result =
        service.signIn(OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, null);

    assertEquals("alice", result.getFirstName());
    assertEquals("", result.getLastName());
    assertFalse(result.getRole().isBlank());
  }

  @Test
  void signIn_bootstrapAdminEmail_unverified_getsUserRole() {
    service = new OAuthAccountService(oauthAccountRepository, userRepository, "admin@example.com");
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.FACEBOOK, "fb-admin"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setUserId(8L);
              return saved;
            });
    when(oauthAccountRepository.save(any(OAuthAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    User result =
        service.signIn(
            OAuthProvider.FACEBOOK, "fb-admin", "admin@example.com", false, "Admin User");

    assertEquals("USER", result.getRole());
  }
}
