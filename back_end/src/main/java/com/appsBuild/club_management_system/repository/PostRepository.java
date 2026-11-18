package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.Post;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  /**
   * Find a post by its ID and the club it belongs to.
   * Ensures the post actually belongs to the specified club.
   */
  Optional<Post> findByPostIdAndClub_ClubId(Long postId, Long clubId);

  /**
   * List posts filtered by club name and visible visibility levels (paginated).
   */
  Page<Post> findByClub_ClubNameIgnoreCaseAndVisibilityIn(
      String clubName, Collection<VisibilityLevel> visibilities, Pageable pageable);

  /**
   * List all posts across all clubs filtered by visible visibility levels (paginated).
   */
  Page<Post> findByVisibilityIn(
      Collection<VisibilityLevel> visibilities, Pageable pageable);
}
