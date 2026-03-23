package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.JoinRequest;
import com.appsBuild.club_management_system.repository.JoinRequestRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinRequestServiceImpl {
  private final JoinRequestRepository joinRequestRepository;

  public JoinRequest save(JoinRequest joinRequest) {
    if (joinRequest == null) {
      throw new NoContentException("JoinRequest cannot be null");
    }
    return joinRequestRepository.save(joinRequest);
  }

  public JoinRequest findById(Long id) {
    return joinRequestRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("JoinRequest not found with id: " + id));
  }

  public Optional<JoinRequest> findByIdOptional(Long id) {
    return joinRequestRepository.findById(id);
  }

  public List<JoinRequest> findAll() {
    List<JoinRequest> joinRequests = joinRequestRepository.findAll();
    if (joinRequests.isEmpty()) {
      throw new NoContentException("No join requests found");
    }
    return joinRequests;
  }

  public void delete(Long id) {
    JoinRequest joinRequest = findById(id);
    joinRequestRepository.delete(joinRequest);
  }
}
