package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.dto.post.*;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.*;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.PostRepository;
import com.appsBuild.club_management_system.service.storage.S3ObjectStorageService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PostService {

  private static final List<VisibilityLevel> PUBLIC_VISIBILITIES =
      List.of(
          VisibilityLevel.PUBLIC,
          VisibilityLevel.STUDENTS_ONLY,
          VisibilityLevel.MEMBERS_ONLY);

  private final PostRepository postRepository;
  private final ClubRepository clubRepository;
  private final S3ObjectStorageService s3ObjectStorageService;

  // ── Create ──────────────────────────────────────────────────────────

  @Transactional
  public PostResponse createPost(Long clubId, PostRequest request) {
    Club club =
        clubRepository
            .findById(clubId)
            .orElseThrow(() -> new NotFoundException("Club not found with id: " + clubId));

    Post post =
        Post.builder()
            .title(request.title())
            .description(request.content()) // API 'content' -> Entity 'description'
            .visibility(request.visibility())
            .createdAt(new Date())
            .club(club)
            .attachments(new ArrayList<>())
            .comments(new ArrayList<>())
            .build();

    Post saved = postRepository.save(post);
    return toPostResponse(saved);
  }

  // ── Read ────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public PostDetailResponse getPostDetails(Long postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

    if (post.getVisibility() == VisibilityLevel.HIDDEN) {
      throw new NotFoundException("Post not found with id: " + postId);
    }

    return toPostDetailResponse(post);
  }

  @Transactional(readOnly = true)
  public PostListResponse listPosts(int page, int limit, String clubName) {
    int pageIndex = Math.max(page, 1) - 1; // 1-based API to 0-based Spring Data
    int pageSize = Math.min(Math.max(limit, 1), 100); // clamp 1..100
    Pageable pageable =
        PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Post> postPage;
    if (clubName != null && !clubName.isBlank()) {
      postPage =
          postRepository.findByClub_ClubNameIgnoreCaseAndVisibilityIn(
              clubName.trim(), PUBLIC_VISIBILITIES, pageable);
    } else {
      postPage = postRepository.findByVisibilityIn(PUBLIC_VISIBILITIES, pageable);
    }

    List<PostResponse> postResponses =
        postPage.getContent().stream().map(this::toPostResponse).toList();
    PaginationResponse pagination =
        new PaginationResponse(
            postPage.getNumber() + 1, postPage.getTotalPages(), postPage.getTotalElements());

    return new PostListResponse(postResponses, pagination);
  }

  // ── Update ──────────────────────────────────────────────────────────

  @Transactional
  public PostResponse updatePost(Long clubId, Long postId, PostRequest request) {
    Post post =
        postRepository
            .findByPostIdAndClub_ClubId(postId, clubId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Post not found with id: " + postId + " in club: " + clubId));

    post.setTitle(request.title());
    post.setDescription(request.content());
    post.setVisibility(request.visibility());

    Post updated = postRepository.save(post);
    return toPostResponse(updated);
  }

  // ── Delete ──────────────────────────────────────────────────────────

  @Transactional
  public void deletePost(Long clubId, Long postId) {
    Post post =
        postRepository
            .findByPostIdAndClub_ClubId(postId, clubId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Post not found with id: " + postId + " in club: " + clubId));

    postRepository.delete(post);
  }

  // ── Moderation (Hide) ───────────────────────────────────────────────

  @Transactional
  public PostResponse hidePost(Long postId, HidePostRequest request) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

    post.setVisibility(VisibilityLevel.HIDDEN);
    post.setHiddenReason(request.reason());

    Post updated = postRepository.save(post);
    return toPostResponse(updated);
  }

  // ── Mapping Helpers ─────────────────────────────────────────────────

  private PostResponse toPostResponse(Post post) {
    List<AttachmentResponse> attachments =
        post.getAttachments() != null
            ? post.getAttachments().stream().map(this::toAttachmentResponse).toList()
            : List.of();

    return new PostResponse(
        post.getPostId(),
        post.getTitle(),
        post.getDescription(),
        toClubReference(post.getClub()),
        post.getCreatedAt(),
        post.getVisibility(),
        attachments);
  }

  private PostDetailResponse toPostDetailResponse(Post post) {
    List<AttachmentResponse> attachments =
        post.getAttachments() != null
            ? post.getAttachments().stream().map(this::toAttachmentResponse).toList()
            : List.of();

    List<CommentResponse> comments =
        post.getComments() != null
            ? post.getComments().stream().map(this::toCommentResponse).toList()
            : List.of();

    return new PostDetailResponse(
        post.getPostId(),
        post.getTitle(),
        post.getDescription(),
        toClubReference(post.getClub()),
        post.getCreatedAt(),
        post.getVisibility(),
        attachments,
        comments);
  }

  private ClubReferenceResponse toClubReference(Club club) {
    if (club == null) return null;
    String picUrl =
        club.getProfilePicture() != null
            ? s3ObjectStorageService.presignGetUrl(club.getProfilePicture().getS3Key())
            : null;
    return new ClubReferenceResponse(
        club.getClubId(),
        club.getClubName(),
        club.getDescription(),
        new ClubReferenceResponse.ProfilePictureRef(picUrl));
  }

  private AttachmentResponse toAttachmentResponse(Attachment attachment) {
    String url =
        attachment.getS3Key() != null
            ? s3ObjectStorageService.presignGetUrl(attachment.getS3Key())
            : null;
    return new AttachmentResponse(attachment.getAttachmentId(), attachment.getType(), url);
  }

  private CommentResponse toCommentResponse(Comment comment) {
    User user = comment.getUser();
    String userPicUrl =
        user != null && user.getProfilePicture() != null
            ? s3ObjectStorageService.presignGetUrl(user.getProfilePicture().getS3Key())
            : null;

    UserReferenceResponse userRef =
        user != null
            ? new UserReferenceResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                new UserReferenceResponse.ProfilePictureRef(userPicUrl))
            : null;

    return new CommentResponse(
        comment.getCommentId(), comment.getContent(), userRef, comment.getCreatedAt());
  }
}
