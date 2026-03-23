package com.appsBuild.club_management_system.model.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Club {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long clubId;

  @Column(name = "club_name", nullable = false, length = 100)
  private String clubName;

  @Column(name = "club_full_name", nullable = true, length = 200)
  private String clubFullName;

  @Column(name = "description", nullable = true, length = 1000)
  private String description;

  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ClubMembership> memberships;

  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ClubMembershipHistory> membershipHistory;

  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<JoinRequest> joinRequests;

  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Post> posts;

  @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Event> events;
}
