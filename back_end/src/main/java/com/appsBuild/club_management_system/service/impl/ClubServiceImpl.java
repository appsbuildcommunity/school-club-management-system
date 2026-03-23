package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.repository.ClubRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl {
  private final ClubRepository clubRepository;

  public Club save(Club club) {
    if (club == null) {
      throw new NoContentException("Club cannot be null");
    }
    return clubRepository.save(club);
  }

  public Club findById(Long id) {
    return clubRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Club not found with id: " + id));
  }

  public Optional<Club> findByIdOptional(Long id) {
    return clubRepository.findById(id);
  }

  public List<Club> findAll() {
    List<Club> clubs = clubRepository.findAll();
    if (clubs.isEmpty()) {
      throw new NoContentException("No clubs found");
    }
    return clubs;
  }

  public void delete(Long id) {
    Club club = findById(id);
    clubRepository.delete(club);
  }
}
