package com.appsBuild.club_management_system.model.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @Column(name = "username", nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "keycloak_sub", nullable = false, unique = true, length = 100)
  private String keycloakSub;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<ClubMembershipHistory> membershipHistory;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<ClubMembership> clubMemberships;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<JoinRequest> joinRequests;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_picture_id", referencedColumnName = "profilePictureId")
  private ProfilePicture profilePicture;
}
