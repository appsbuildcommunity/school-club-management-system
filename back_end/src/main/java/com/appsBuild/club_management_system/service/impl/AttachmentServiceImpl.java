package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Attachment;
import com.appsBuild.club_management_system.repository.AttachmentRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl {
  private final AttachmentRepository attachmentRepository;

  public Attachment save(Attachment attachment) {
    if (attachment == null) {
      throw new NoContentException("Attachment cannot be null");
    }
    return attachmentRepository.save(attachment);
  }

  public Attachment findById(Long id) {
    return attachmentRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Attachment not found with id: " + id));
  }

  public Optional<Attachment> findByIdOptional(Long id) {
    return attachmentRepository.findById(id);
  }

  public List<Attachment> findAll() {
    List<Attachment> attachments = attachmentRepository.findAll();
    if (attachments.isEmpty()) {
      throw new NoContentException("No attachments found");
    }
    return attachments;
  }

  public void delete(Long id) {
    Attachment attachment = findById(id);
    attachmentRepository.delete(attachment);
  }
}
