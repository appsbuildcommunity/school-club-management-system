package com.appsBuild.club_management_system.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "club_membership_endpoint",
    uniqueConstraints = @UniqueConstraint(columnNames = {"membership_id", "endpoint_id"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubMembershipEndpoint {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id", nullable = false)
  private ClubMembership clubMembership;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "endpoint_id", nullable = false)
  private Endpoint endpoint;

  @Column(name = "granted_date", nullable = false)
  private Date grantedDate;
}
