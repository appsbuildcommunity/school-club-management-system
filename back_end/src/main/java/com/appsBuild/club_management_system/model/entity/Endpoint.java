package com.appsBuild.club_management_system.model.entity;

import com.appsBuild.club_management_system.model.enums.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Endpoint {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long endpointId;

  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String name;

  @Column(name = "description", nullable = false, length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 50)
  private Category category;

  @Column(name = "privileged", nullable = false)
  private boolean privileged;
}
