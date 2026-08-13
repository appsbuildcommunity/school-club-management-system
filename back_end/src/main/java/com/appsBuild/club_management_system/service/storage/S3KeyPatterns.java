package com.appsBuild.club_management_system.service.storage;

import java.util.UUID;

public final class S3KeyPatterns {

  private static final String USER_PROFILE = "users/\\d+/[^/]+\\.\\w+";
  private static final String CLUB_PROFILE = "clubs/\\d+/[^/]+\\.\\w+";
  private static final String EVENT_ATTACHMENT = "clubs/\\d+/events/\\d+/.+\\.\\w+";
  private static final String POST_ATTACHMENT = "clubs/\\d+/posts/\\d+/.+\\.\\w+";

  private S3KeyPatterns() {}

  public static boolean isUserProfilePicture(String key) {
    return key.matches(USER_PROFILE);
  }

  public static boolean isClubProfilePicture(String key) {
    return key.matches(CLUB_PROFILE);
  }

  public static boolean isProfilePicture(String key) {
    return isUserProfilePicture(key) || isClubProfilePicture(key);
  }

  public static boolean isEventAttachment(String key) {
    return key.matches(EVENT_ATTACHMENT);
  }

  public static boolean isPostAttachment(String key) {
    return key.matches(POST_ATTACHMENT);
  }

  public static boolean isAttachment(String key) {
    return isEventAttachment(key) || isPostAttachment(key);
  }

  public static String userProfilePictureKey(Long userId, String extension) {
    return String.format("users/%d/%s.%s", userId, UUID.randomUUID(), extension);
  }

  public static String clubProfilePictureKey(Long clubId, String extension) {
    return String.format("clubs/%d/%s.%s", clubId, UUID.randomUUID(), extension);
  }

  public static String eventAttachmentKey(Long clubId, Long eventId, String extension) {
    return String.format(
        "clubs/%d/events/%d/%s.%s", clubId, eventId, UUID.randomUUID(), extension);
  }

  public static String postAttachmentKey(Long clubId, Long postId, String extension) {
    return String.format(
        "clubs/%d/posts/%d/%s.%s", clubId, postId, UUID.randomUUID(), extension);
  }

  public static long userIdFrom(String key) {
    return Long.parseLong(key.split("/")[1]);
  }

  public static long clubIdFrom(String key) {
    return Long.parseLong(key.split("/")[1]);
  }

  public static long eventIdFrom(String key) {
    return Long.parseLong(key.split("/")[3]);
  }

  public static long postIdFrom(String key) {
    return Long.parseLong(key.split("/")[3]);
  }
}
