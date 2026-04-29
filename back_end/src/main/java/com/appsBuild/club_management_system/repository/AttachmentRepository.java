package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.Attachment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

  Optional<Attachment> findByS3Key(String oldKey);
}
