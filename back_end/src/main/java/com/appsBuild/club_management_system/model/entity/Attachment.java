package com.appsBuild.club_management_system.model.entity;

import com.appsBuild.club_management_system.model.enums.AttachmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="attachment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attachment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long attachmentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private AttachmentType type;

  @Column(name = "s3_key", nullable = false, length = 500)
  private String s3Key;
}
