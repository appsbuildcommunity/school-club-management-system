package com.appsBuild.club_management_system.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assistant_member_privilege")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssistantMemberPrivilege {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long privilegeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "endpoint_id", nullable = false)
  private Endpoint endpoint;

  @Column(name = "granted_date", nullable = false)
  private Date grantedDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id", nullable = false)
  private ClubMembership clubMembership;
}
