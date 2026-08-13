package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

  // Finds the profile picture belonging to the given user, if one exists.
  Optional<ProfilePicture> findByUser_UserId(Long userId);

  // Finds the profile picture stored under the given S3 key, if one exists (used when replacing an old picture).
  Optional<ProfilePicture> findByS3Key(String oldKey);
}
