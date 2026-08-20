package com.appsBuild.club_management_system.model.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long clubProfileId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "club_profile_endpoints",
      joinColumns = @JoinColumn(name = "club_profile_id"),
      inverseJoinColumns = @JoinColumn(name = "endpoint_id"))
  private List<Endpoint> endpoints;
}
