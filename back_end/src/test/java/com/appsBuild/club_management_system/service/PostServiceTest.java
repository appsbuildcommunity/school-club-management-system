package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.appsBuild.club_management_system.dto.post.*;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.*;
import com.appsBuild.club_management_system.model.enums.AttachmentType;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.PostRepository;
import com.appsBuild.club_management_system.service.storage.S3ObjectStorageService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock private PostRepository postRepository;
  @Mock private ClubRepository clubRepository;
  @Mock private S3ObjectStorageService s3ObjectStorageService;

  @InjectMocks private PostService postService;

  private Club testClub;
  private Post testPost;

  @BeforeEach
  void setUp() {
    testClub =
        Club.builder()
            .clubId(1L)
            .clubName("Robotics")
            .clubFullName("Robotics Club")
            .description("Build robots")
            .build();

    testPost =
        Post.builder()
            .postId(10L)
            .title("Welcome to Robotics")
            .description("First meeting on Friday")
            .visibility(VisibilityLevel.PUBLIC)
            .createdAt(new Date())
            .club(testClub)
            .attachments(new ArrayList<>())
            .comments(new ArrayList<>())
            .build();
  }

  // ── createPost ──────────────────────────────────────────────────────

  @Test
  void createPost_success() {
    when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
    when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
      Post p = inv.getArgument(0);
      p.setPostId(10L);
      return p;
    });

    PostRequest req = new PostRequest("Welcome to Robotics", "First meeting on Friday", VisibilityLevel.PUBLIC);
    PostResponse resp = postService.createPost(1L, req);

    assertNotNull(resp);
    assertEquals(10L, resp.postId());
    assertEquals("Welcome to Robotics", resp.title());
    assertEquals("First meeting on Friday", resp.content());
    assertEquals(VisibilityLevel.PUBLIC, resp.visibility());
    assertEquals(1L, resp.club().id());
  }

  @Test
  void createPost_clubNotFound_throwsNotFound() {
    when(clubRepository.findById(99L)).thenReturn(Optional.empty());

    PostRequest req = new PostRequest("Title", "Content", VisibilityLevel.PUBLIC);
    assertThrows(NotFoundException.class, () -> postService.createPost(99L, req));
  }

  // ── getPostDetails ──────────────────────────────────────────────────

  @Test
  void getPostDetails_success() {
    User commentUser = User.builder().userId(5L).username("alice").firstName("Alice").lastName("Smith").email("alice@test.com").build();
    Comment comment = Comment.builder().commentId(100L).content("Great post!").createdAt(new Date()).user(commentUser).build();
    testPost.setComments(List.of(comment));

    Attachment attachment = Attachment.builder().attachmentId(50L).type(AttachmentType.IMAGE).s3Key("clubs/1/posts/10/img.png").build();
    testPost.setAttachments(List.of(attachment));

    when(postRepository.findById(10L)).thenReturn(Optional.of(testPost));
    when(s3ObjectStorageService.presignGetUrl("clubs/1/posts/10/img.png")).thenReturn("https://s3.example.com/img.png");

    PostDetailResponse resp = postService.getPostDetails(10L);

    assertNotNull(resp);
    assertEquals(10L, resp.postId());
    assertEquals(1, resp.comments().size());
    assertEquals("Great post!", resp.comments().get(0).content());
    assertEquals(1, resp.attachments().size());
    assertEquals("https://s3.example.com/img.png", resp.attachments().get(0).url());
  }

  @Test
  void getPostDetails_hiddenPost_throwsNotFound() {
    testPost.setVisibility(VisibilityLevel.HIDDEN);
    when(postRepository.findById(10L)).thenReturn(Optional.of(testPost));

    assertThrows(NotFoundException.class, () -> postService.getPostDetails(10L));
  }

  // ── listPosts ───────────────────────────────────────────────────────

  @Test
  void listPosts_withoutClubFilter() {
    Page<Post> page = new PageImpl<>(List.of(testPost));
    when(postRepository.findByVisibilityIn(anyCollection(), any(Pageable.class))).thenReturn(page);

    PostListResponse resp = postService.listPosts(1, 20, null);

    assertNotNull(resp);
    assertEquals(1, resp.posts().size());
    assertEquals(1, resp.pagination().currentPage());
    assertEquals(1, resp.pagination().totalItems());
  }

  @Test
  void listPosts_withClubFilter() {
    Page<Post> page = new PageImpl<>(List.of(testPost));
    when(postRepository.findByClub_ClubNameIgnoreCaseAndVisibilityIn(eq("Robotics"), anyCollection(), any(Pageable.class)))
        .thenReturn(page);

    PostListResponse resp = postService.listPosts(1, 20, "Robotics");

    assertNotNull(resp);
    assertEquals(1, resp.posts().size());
    verify(postRepository).findByClub_ClubNameIgnoreCaseAndVisibilityIn(eq("Robotics"), anyCollection(), any(Pageable.class));
  }

  // ── updatePost ──────────────────────────────────────────────────────

  @Test
  void updatePost_success() {
    when(postRepository.findByPostIdAndClub_ClubId(10L, 1L)).thenReturn(Optional.of(testPost));
    when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

    PostRequest req = new PostRequest("Updated Title", "Updated Content", VisibilityLevel.MEMBERS_ONLY);
    PostResponse resp = postService.updatePost(1L, 10L, req);

    assertNotNull(resp);
    assertEquals("Updated Title", resp.title());
    assertEquals("Updated Content", resp.content());
    assertEquals(VisibilityLevel.MEMBERS_ONLY, resp.visibility());
  }

  @Test
  void updatePost_notFound_throwsNotFound() {
    when(postRepository.findByPostIdAndClub_ClubId(10L, 1L)).thenReturn(Optional.empty());

    PostRequest req = new PostRequest("Updated Title", "Updated Content", VisibilityLevel.MEMBERS_ONLY);
    assertThrows(NotFoundException.class, () -> postService.updatePost(1L, 10L, req));
  }

  // ── deletePost ──────────────────────────────────────────────────────

  @Test
  void deletePost_success() {
    when(postRepository.findByPostIdAndClub_ClubId(10L, 1L)).thenReturn(Optional.of(testPost));

    postService.deletePost(1L, 10L);

    verify(postRepository).delete(testPost);
  }

  @Test
  void deletePost_notFound_throwsNotFound() {
    when(postRepository.findByPostIdAndClub_ClubId(10L, 1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> postService.deletePost(1L, 10L));
  }

  // ── hidePost ────────────────────────────────────────────────────────

  @Test
  void hidePost_success() {
    when(postRepository.findById(10L)).thenReturn(Optional.of(testPost));
    when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

    HidePostRequest req = new HidePostRequest("Inappropriate content");
    PostResponse resp = postService.hidePost(10L, req);

    assertNotNull(resp);
    assertEquals(VisibilityLevel.HIDDEN, resp.visibility());
    assertEquals(VisibilityLevel.HIDDEN, testPost.getVisibility());
    assertEquals("Inappropriate content", testPost.getHiddenReason());
  }

  @Test
  void hidePost_notFound_throwsNotFound() {
    when(postRepository.findById(99L)).thenReturn(Optional.empty());

    HidePostRequest req = new HidePostRequest("Reason");
    assertThrows(NotFoundException.class, () -> postService.hidePost(99L, req));
  }
}
