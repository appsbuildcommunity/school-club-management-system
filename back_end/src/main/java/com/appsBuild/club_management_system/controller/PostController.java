package com.appsBuild.club_management_system.controller;

import com.appsBuild.club_management_system.dto.post.*;
import com.appsBuild.club_management_system.service.PostService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class PostController {

  private final PostService postService;

  // ── Read (Public / Member) ──────────────────────────────────────────

  @GetMapping("/api/posts")
  public ResponseEntity<PostListResponse> listPosts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(required = false) String club_name) {
    return ResponseEntity.ok(postService.listPosts(page, limit, club_name));
  }

  @GetMapping("/api/posts/{postId}")
  public ResponseEntity<PostDetailResponse> getPostDetails(@PathVariable Long postId) {
    return ResponseEntity.ok(postService.getPostDetails(postId));
  }

  // ── Club Post Management (President / Assistant with MANAGE_POSTS) ──

  @PostMapping("/api/clubs/{clubId}/posts")
  // @Grantable(
  //     name = "create_post",
  //     description = "Create a post in a club",
  //     category = Category.MANAGE_POSTS,
  //     privileged = false)
  // @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'create_post')")
  public ResponseEntity<PostResponse> createPost(
      @PathVariable Long clubId, @Valid @RequestBody PostRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(postService.createPost(clubId, request));
  }

  @PutMapping("/api/clubs/{clubId}/posts/{postId}")
  // @Grantable(
  //     name = "update_post",
  //     description = "Update a post in a club",
  //     category = Category.MANAGE_POSTS,
  //     privileged = false)
  // @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'update_post')")
  public ResponseEntity<PostResponse> updatePost(
      @PathVariable Long clubId,
      @PathVariable Long postId,
      @Valid @RequestBody PostRequest request) {
    return ResponseEntity.ok(postService.updatePost(clubId, postId, request));
  }

  @DeleteMapping("/api/clubs/{clubId}/posts/{postId}")
  // @Grantable(
  //     name = "delete_post",
  //     description = "Delete a post in a club",
  //     category = Category.MANAGE_POSTS,
  //     privileged = false)
  // @PreAuthorize("hasRole('ADMIN') or @clubAccess.hasEndpoint(#clubId, 'delete_post')")
  public ResponseEntity<Void> deletePost(
      @PathVariable Long clubId, @PathVariable Long postId) {
    postService.deletePost(clubId, postId);
    return ResponseEntity.noContent().build();
  }

  // ── Moderation (Hide Post) ──────────────────────────────────────────

  @PutMapping("/api/posts/{postId}/hide")
  // @PreAuthorize("hasRole('ADMIN') or hasRole('CLUBS_RESPONSIBLE')")
  public ResponseEntity<PostResponse> hidePost(
      @PathVariable Long postId, @Valid @RequestBody HidePostRequest request) {
    return ResponseEntity.ok(postService.hidePost(postId, request));
  }
}
