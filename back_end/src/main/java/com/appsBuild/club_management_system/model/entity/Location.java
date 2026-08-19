package com.appsBuild.club_management_system.model.entity;

import java.util.List;

import com.appsBuild.club_management_system.model.enums.LocationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long locationId;

  @Column(name = "label", nullable = false, length = 100)
  private String label;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private LocationType type;

  @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
  private List<LocationReservation> reservations;
}
