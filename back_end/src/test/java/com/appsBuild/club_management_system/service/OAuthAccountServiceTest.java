package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import java.util.Date;
import java.util.Optional;
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
  void signIn_usernameCollision_appendsSuffix() {
    when(oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, "google-sub"))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(User.builder().userId(1L).build()));
    when(userRepository.findByUsername("alice-2")).thenReturn(Optional.empty());
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
        service.signIn(OAuthProvider.GOOGLE, "google-sub", "alice@example.com", true, "Alice Doe");

    assertEquals("alice-2", result.getUsername());
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
}
