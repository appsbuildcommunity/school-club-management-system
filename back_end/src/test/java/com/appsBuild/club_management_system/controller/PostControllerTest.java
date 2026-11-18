package com.appsBuild.club_management_system.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.appsBuild.club_management_system.dto.post.*;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import com.appsBuild.club_management_system.service.ClubAccessService;
import com.appsBuild.club_management_system.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableWebSecurity
class PostControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private PostService postService;
  @MockitoBean(name = "clubAccess") private ClubAccessService clubAccessService;

  @BeforeEach
  void setUp() {
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "test-user")
            .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }

  // ── listPosts ───────────────────────────────────────────────────────

  @Test
  void listPosts_returns200() throws Exception {
    PostResponse post =
        new PostResponse(
            1L, "Title", "Content", null, new Date(), VisibilityLevel.PUBLIC, List.of());
    PostListResponse listResponse =
        new PostListResponse(List.of(post), new PaginationResponse(1, 1, 1));

    when(postService.listPosts(1, 20, null)).thenReturn(listResponse);

    mockMvc
        .perform(get("/api/posts?page=1&limit=20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.posts").isArray())
        .andExpect(jsonPath("$.posts[0].postId").value(1))
        .andExpect(jsonPath("$.pagination.totalItems").value(1));
  }

  // ── getPostDetails ──────────────────────────────────────────────────

  @Test
  void getPostDetails_returns200() throws Exception {
    PostDetailResponse detail =
        new PostDetailResponse(
            1L, "Title", "Content", null, new Date(), VisibilityLevel.PUBLIC, List.of(), List.of());

    when(postService.getPostDetails(1L)).thenReturn(detail);

    mockMvc
        .perform(get("/api/posts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.postId").value(1))
        .andExpect(jsonPath("$.title").value("Title"));
  }

  // ── createPost ──────────────────────────────────────────────────────

  @Test
  void createPost_returns201() throws Exception {
    PostResponse post =
        new PostResponse(
            1L, "Title", "Content", null, new Date(), VisibilityLevel.PUBLIC, List.of());

    when(postService.createPost(eq(1L), any(PostRequest.class))).thenReturn(post);

    PostRequest req = new PostRequest("Title", "Content", VisibilityLevel.PUBLIC);

    mockMvc
        .perform(
            post("/api/clubs/1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.postId").value(1))
        .andExpect(jsonPath("$.title").value("Title"));
  }

  @Test
  void createPost_invalidBody_returns400() throws Exception {
    PostRequest invalidReq = new PostRequest("", "", null);

    mockMvc
        .perform(
            post("/api/clubs/1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
        .andExpect(status().isBadRequest());
  }

  // ── updatePost ──────────────────────────────────────────────────────

  @Test
  void updatePost_returns200() throws Exception {
    PostResponse post =
        new PostResponse(
            1L, "Updated Title", "Updated Content", null, new Date(), VisibilityLevel.PUBLIC, List.of());

    when(postService.updatePost(eq(1L), eq(1L), any(PostRequest.class))).thenReturn(post);

    PostRequest req = new PostRequest("Updated Title", "Updated Content", VisibilityLevel.PUBLIC);

    mockMvc
        .perform(
            put("/api/clubs/1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"));
  }

  // ── deletePost ──────────────────────────────────────────────────────

  @Test
  void deletePost_returns204() throws Exception {
    doNothing().when(postService).deletePost(1L, 1L);

    mockMvc
        .perform(delete("/api/clubs/1/posts/1"))
        .andExpect(status().isNoContent());

    verify(postService).deletePost(1L, 1L);
  }

  // ── hidePost ────────────────────────────────────────────────────────

  @Test
  void hidePost_returns200() throws Exception {
    PostResponse post =
        new PostResponse(
            1L, "Title", "Content", null, new Date(), VisibilityLevel.HIDDEN, List.of());

    when(postService.hidePost(eq(1L), any(HidePostRequest.class))).thenReturn(post);

    HidePostRequest req = new HidePostRequest("Offensive language");

    mockMvc
        .perform(
            put("/api/posts/1/hide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.visibility").value("HIDDEN"));
  }
}
