package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Post;
import com.appsBuild.club_management_system.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostServiceImpl {
  private final PostRepository postRepository;

  public Post save(Post post) {
    if (post == null) {
      throw new NoContentException("Post cannot be null");
    }
    return postRepository.save(post);
  }

  public Post findById(Long id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Post not found with id: " + id));
  }

  public Optional<Post> findByIdOptional(Long id) {
    return postRepository.findById(id);
  }

  public List<Post> findAll() {
    List<Post> posts = postRepository.findAll();
    if (posts.isEmpty()) {
      throw new NoContentException("No posts found");
    }
    return posts;
  }

  public void delete(Long id) {
    Post post = findById(id);
    postRepository.delete(post);
  }
}
