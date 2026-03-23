package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.ClubMembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubMembershipHistoryRepository extends JpaRepository<ClubMembershipHistory, Long> {
}
