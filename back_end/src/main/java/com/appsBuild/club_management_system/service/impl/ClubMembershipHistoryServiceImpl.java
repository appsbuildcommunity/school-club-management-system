package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.ClubMembershipHistory;
import com.appsBuild.club_management_system.repository.ClubMembershipHistoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubMembershipHistoryServiceImpl {
  private final ClubMembershipHistoryRepository clubMembershipHistoryRepository;

  public ClubMembershipHistory save(ClubMembershipHistory clubMembershipHistory) {
    if (clubMembershipHistory == null) {
      throw new NoContentException("ClubMembershipHistory cannot be null");
    }
    return clubMembershipHistoryRepository.save(clubMembershipHistory);
  }

  public ClubMembershipHistory findById(Long id) {
    return clubMembershipHistoryRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("ClubMembershipHistory not found with id: " + id));
  }

  public Optional<ClubMembershipHistory> findByIdOptional(Long id) {
    return clubMembershipHistoryRepository.findById(id);
  }

  public List<ClubMembershipHistory> findAll() {
    List<ClubMembershipHistory> clubMembershipHistories = clubMembershipHistoryRepository.findAll();
    if (clubMembershipHistories.isEmpty()) {
      throw new NoContentException("No club membership histories found");
    }
    return clubMembershipHistories;
  }

  public void delete(Long id) {
    ClubMembershipHistory clubMembershipHistory = findById(id);
    clubMembershipHistoryRepository.delete(clubMembershipHistory);
  }
}
