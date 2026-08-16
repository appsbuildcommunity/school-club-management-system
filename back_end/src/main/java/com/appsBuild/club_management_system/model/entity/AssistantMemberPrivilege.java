package com.appsBuild.club_management_system.model.entity;

import com.appsBuild.club_management_system.model.enums.Privilege;
import jakarta.persistence.*;

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

  @Enumerated(EnumType.STRING)
  @Column(name = "privilege", nullable = false, length = 50)
  private Privilege privilege;

  @Column(name = "granted_date", nullable = false)
  private Date grantedDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id", nullable = false)
  private ClubMembership clubMembership;
}
