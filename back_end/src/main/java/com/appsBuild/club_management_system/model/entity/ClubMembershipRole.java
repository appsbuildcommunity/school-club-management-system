package com.appsBuild.club_management_system.model.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class ClubMembershipRole {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id", nullable = false)
  private ClubMembership clubMembership;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_role_id", nullable = false)
  private ClubRole clubRole;

  @Column(name = "assigned_date", nullable = false)
  private Date assignedDate;
}
