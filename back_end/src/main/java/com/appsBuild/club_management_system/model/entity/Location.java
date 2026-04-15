package com.appsBuild.club_management_system.model.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "internal_location", nullable = false)
  private boolean internalLocation;

  @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<LocationReservation> reservations;
}
