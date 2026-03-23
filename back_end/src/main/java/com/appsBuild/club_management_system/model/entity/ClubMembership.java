package com.appsBuild.club_management_system.model.entity;

import java.util.Date;
import java.util.List;

import com.appsBuild.club_management_system.model.enums.ClubRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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

  @Column(name = "joined_date", nullable = false)
  private Date joinedDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  @OneToMany(mappedBy = "clubMembership", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<AssistantMemberPrivilege> privileges;
}
