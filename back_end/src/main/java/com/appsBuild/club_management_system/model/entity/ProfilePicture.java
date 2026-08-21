package com.appsBuild.club_management_system.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "profile_picture")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfilePicture {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long profilePictureId;

  @Column(name = "s3_key", nullable = false, length = 500)
  private String s3Key;
}
