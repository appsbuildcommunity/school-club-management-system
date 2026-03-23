package com.appsBuild.club_management_system.model.entity;

import com.appsBuild.club_management_system.model.enums.Privilege;
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
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
