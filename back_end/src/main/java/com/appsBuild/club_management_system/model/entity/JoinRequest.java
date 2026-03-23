package com.appsBuild.club_management_system.model.entity;

import com.appsBuild.club_management_system.model.enums.DemandStatus;
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
