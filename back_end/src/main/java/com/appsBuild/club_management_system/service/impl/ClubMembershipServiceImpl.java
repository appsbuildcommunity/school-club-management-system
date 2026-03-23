package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.ClubMembership;
import com.appsBuild.club_management_system.repository.ClubMembershipRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubMembershipServiceImpl {
  private final ClubMembershipRepository clubMembershipRepository;

  public ClubMembership save(ClubMembership clubMembership) {
    if (clubMembership == null) {
      throw new NoContentException("ClubMembership cannot be null");
    }
    return clubMembershipRepository.save(clubMembership);
  }

  public ClubMembership findById(Long id) {
    return clubMembershipRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("ClubMembership not found with id: " + id));
  }

  public Optional<ClubMembership> findByIdOptional(Long id) {
    return clubMembershipRepository.findById(id);
  }

  public List<ClubMembership> findAll() {
    List<ClubMembership> clubMemberships = clubMembershipRepository.findAll();
    if (clubMemberships.isEmpty()) {
      throw new NoContentException("No club memberships found");
    }
    return clubMemberships;
  }

  public void delete(Long id) {
    ClubMembership clubMembership = findById(id);
    clubMembershipRepository.delete(clubMembership);
  }
}
