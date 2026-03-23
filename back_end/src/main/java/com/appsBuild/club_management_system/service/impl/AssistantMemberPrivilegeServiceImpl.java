package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.AssistantMemberPrivilege;
import com.appsBuild.club_management_system.repository.AssistantMemberPrivilegeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssistantMemberPrivilegeServiceImpl {
  private final AssistantMemberPrivilegeRepository assistantMemberPrivilegeRepository;

  public AssistantMemberPrivilege save(AssistantMemberPrivilege assistantMemberPrivilege) {
    if (assistantMemberPrivilege == null) {
      throw new NoContentException("AssistantMemberPrivilege cannot be null");
    }
    return assistantMemberPrivilegeRepository.save(assistantMemberPrivilege);
  }

  public AssistantMemberPrivilege findById(Long id) {
    return assistantMemberPrivilegeRepository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("AssistantMemberPrivilege not found with id: " + id));
  }

  public Optional<AssistantMemberPrivilege> findByIdOptional(Long id) {
    return assistantMemberPrivilegeRepository.findById(id);
  }

  public List<AssistantMemberPrivilege> findAll() {
    List<AssistantMemberPrivilege> assistantMemberPrivileges =
        assistantMemberPrivilegeRepository.findAll();
    if (assistantMemberPrivileges.isEmpty()) {
      throw new NoContentException("No assistant member privileges found");
    }
    return assistantMemberPrivileges;
  }

  public void delete(Long id) {
    AssistantMemberPrivilege assistantMemberPrivilege = findById(id);
    assistantMemberPrivilegeRepository.delete(assistantMemberPrivilege);
  }
}
