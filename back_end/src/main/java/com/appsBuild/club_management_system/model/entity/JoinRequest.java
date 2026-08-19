package com.appsBuild.club_management_system.model.entity;

import java.util.Date;

import com.appsBuild.club_management_system.model.enums.DemandStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "join_request")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long joinRequestId;

  @Column(name = "join_request_date", nullable = false)
  private Date joinRequestDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "join_request_status", nullable = false, length = 20)
  private DemandStatus joinRequestStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;
}
