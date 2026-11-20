// Test cases for spring-authorization.yml, run with: semgrep --test .semgrep/
// "ruleid" marks an endpoint that must be reported, "ok" one that must not.

@RestController
@RequestMapping("/api/clubs/{clubId}/posts")
class SemgrepAuthorizationRuleTest {

  // ok: grantable-endpoint-without-preauthorize, write-endpoint-without-preauthorize
  @PostMapping
  @Grantable(name = "manage_posts", description = "d", category = Category.MANAGE_POSTS, privileged = false)
  @PreAuthorize("@clubAccess.hasEndpoint(#clubId, 'manage_posts')")
  public ResponseEntity<Void> protectedGrantable(@PathVariable Long clubId) {
    return null;
  }

  // ruleid: grantable-endpoint-without-preauthorize, write-endpoint-without-preauthorize
  @PutMapping("/{postId}")
  @Grantable(name = "manage_posts", description = "d", category = Category.MANAGE_POSTS, privileged = false)
  public ResponseEntity<Void> grantableWithoutCheck(@PathVariable Long clubId) {
    return null;
  }

  // The annotations below are commented out, so the endpoint is unprotected.
  // ruleid: write-endpoint-without-preauthorize
  @DeleteMapping("/{postId}")
  // @Grantable(name = "manage_posts", description = "d", category = Category.MANAGE_POSTS, privileged = false)
  // @PreAuthorize("@clubAccess.hasEndpoint(#clubId, 'manage_posts')")
  public ResponseEntity<Void> commentedOutCheck(@PathVariable Long clubId) {
    return null;
  }

  // ok: write-endpoint-without-preauthorize
  @PatchMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> explicitSelfService() {
    return null;
  }

  // ok: write-endpoint-without-preauthorize
  @PostMapping("/refresh")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Void> explicitPublic() {
    return null;
  }

  // ruleid: read-endpoint-without-preauthorize
  @GetMapping
  public ResponseEntity<Void> publicRead() {
    return null;
  }

  // ok: read-endpoint-without-preauthorize
  @GetMapping("/{postId}")
  @PreAuthorize("@clubAccess.hasMembership(#clubId)")
  public ResponseEntity<Void> protectedRead(@PathVariable Long clubId) {
    return null;
  }
}
