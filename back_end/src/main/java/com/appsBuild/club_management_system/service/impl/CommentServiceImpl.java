package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Comment;
import com.appsBuild.club_management_system.repository.CommentRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {
  private final CommentRepository commentRepository;

  public Comment save(Comment comment) {
    if (comment == null) {
      throw new NoContentException("Comment cannot be null");
    }
    return commentRepository.save(comment);
  }

  public Comment findById(Long id) {
    return commentRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Comment not found with id: " + id));
  }

  public Optional<Comment> findByIdOptional(Long id) {
    return commentRepository.findById(id);
  }

  public List<Comment> findAll() {
    List<Comment> comments = commentRepository.findAll();
    if (comments.isEmpty()) {
      throw new NoContentException("No comments found");
    }
    return comments;
  }

  public void delete(Long id) {
    Comment comment = findById(id);
    commentRepository.delete(comment);
  }
}
