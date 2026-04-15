// package com.appsBuild.club_management_system.webhook.s3.controller;

// import com.appsBuild.club_management_system.webhook.s3.service.S3WebhookMessageService;
// import lombok.AllArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @AllArgsConstructor
// public class S3WebhookController {

//   private final S3WebhookMessageService s3WebhookMessageService;

//   @PostMapping("/webhook/s3/put")
//   public ResponseEntity<Void> handleS3PutEvent(@RequestBody String rawBody) {
//     s3WebhookMessageService.handleMessage(rawBody);
//     return ResponseEntity.ok().build();
//   }
// }
