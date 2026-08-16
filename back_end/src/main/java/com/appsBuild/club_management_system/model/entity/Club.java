package com.appsBuild.club_management_system.model.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="club")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Club {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "club_id", nullable = false)
  private Long clubId;

  @Column(name = "club_name", nullable = false, length = 100)
  private String clubName;

  @Column(name = "club_full_name", nullable = true, length = 200)
  private String clubFullName;

  @Column(name = "description", nullable = true, length = 1000)
  private String description;

  @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
  private List<ClubMembership> memberships;

  @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
  private List<ClubMembershipHistory> membershipHistory;

  @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
  private List<JoinRequest> joinRequests;

  @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
  private List<Post> posts;

  @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
  private List<Event> events;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_picture_id", referencedColumnName = "profilePictureId")
  private ProfilePicture profilePicture;
}
