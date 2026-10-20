package com.appsBuild.club_management_system.config;

import static org.junit.jupiter.api.Assertions.*;

import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.model.entity.ClubMembershipEndpoint;
import com.appsBuild.club_management_system.model.entity.Endpoint;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.Category;
import com.appsBuild.club_management_system.model.enums.ClubRole;
import com.appsBuild.club_management_system.repository.ClubMembershipEndpointRepository;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EndpointRepository;
import com.appsBuild.club_management_system.repository.UserRepository;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EndpointRegistrarTest {

  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  @BeforeAll
  static void beforeAll() {
    postgres.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private EndpointRegistrar endpointRegistrar;
  @Autowired private EndpointRepository endpointRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ClubRepository clubRepository;
  @Autowired private ClubMembershipRepository clubMembershipRepository;
  @Autowired private ClubMembershipEndpointRepository clubMembershipEndpointRepository;

  @Test
  void run_prunesEndpointRowsWithoutAnnotation() {
    endpointRepository.save(
        Endpoint.builder()
            .name("removed_endpoint")
            .description("No longer present in code")
            .category(Category.MANAGE_EVENTS)
            .privileged(false)
            .build());

    endpointRegistrar.run(null);

    assertTrue(endpointRepository.findByName("removed_endpoint").isEmpty());
    // endpoints registered from @GrantableEndpoint annotations survive
    assertTrue(endpointRepository.findByName("create_club").isPresent());
  }

  @Test
  void run_prunesEndpointThatStillHoldsMemberGrants() {
    User user =
        userRepository.save(
            User.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alice")
                .email("alice@example.com")
                .keycloakSub("sub-" + System.nanoTime())
                .createdAt(new Date())
                .build());
    Club club =
        clubRepository.save(
            Club.builder()
                .clubName("chess-" + System.nanoTime())
                .isCoordinationClub(false)
                .build());
    ClubMembership membership =
        clubMembershipRepository.save(
            ClubMembership.builder()
                .clubRole(ClubRole.MEMBER)
                .startedAt(new Date())
                .user(user)
                .club(club)
                .build());
    Endpoint orphan =
        endpointRepository.save(
            Endpoint.builder()
                .name("removed_with_grants")
                .description("No longer present in code")
                .category(Category.MANAGE_EVENTS)
                .privileged(false)
                .build());
    clubMembershipEndpointRepository.save(
        ClubMembershipEndpoint.builder()
            .clubMembership(membership)
            .endpoint(orphan)
            .grantedDate(new Date())
            .build());

    endpointRegistrar.run(null);

    assertTrue(endpointRepository.findByName("removed_with_grants").isEmpty());
    assertTrue(
        clubMembershipEndpointRepository
            .findByClubMembership_MembershipId(membership.getMembershipId())
            .isEmpty(),
        "member grants for the pruned endpoint must cascade away");
  }
}