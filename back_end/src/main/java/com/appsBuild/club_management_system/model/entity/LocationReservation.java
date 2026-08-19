package com.appsBuild.club_management_system.model.entity;

import java.util.Date;

import com.appsBuild.club_management_system.model.enums.DemandStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_reservation")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationReservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long locationReservationId;

  @Column(name = "start_date", nullable = false)
  private Date startDate;

  @Column(name = "end_date", nullable = false)
  private Date endDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private DemandStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  private Location location;
}
