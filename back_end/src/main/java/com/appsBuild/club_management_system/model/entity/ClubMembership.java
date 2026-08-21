package com.appsBuild.club_management_system.model.entity;

import java.util.Date;
import java.util.List;

import com.appsBuild.club_management_system.model.enums.ClubRole;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "club_membership")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubMembership {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long membershipId;

  @Enumerated(EnumType.STRING)
  @Column(name = "club_role", nullable = false, length = 30)
  private ClubRole clubRole;

  @Column(name = "role_title", nullable = true, length = 100)
  private String roleTitle;

  @Column(name = "role_description", nullable = true, length = 255)
  private String roleDescription;

  @Column(name = "started_at", nullable = false)
  private Date startedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  @OneToMany(mappedBy = "clubMembership", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ClubMembershipProfile> assignedProfiles;

  @OneToMany(mappedBy = "clubMembership", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
  private List<ClubMembershipEndpoint> individualPrivileges;
}
