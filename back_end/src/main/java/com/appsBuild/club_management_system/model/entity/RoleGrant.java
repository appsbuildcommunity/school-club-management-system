package com.appsBuild.club_management_system.model.entity;

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
public class RoleGrant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long roleGrantId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_profile_id", nullable = true)
  private ClubProfile clubProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assistant_member_privilege_id", nullable = false)
  private AssistantMemberPrivilege grant;
}
