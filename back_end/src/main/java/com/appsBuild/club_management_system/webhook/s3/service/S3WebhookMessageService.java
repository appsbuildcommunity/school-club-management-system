package com.appsBuild.club_management_system.webhook.s3.service;

import com.amazonaws.services.sns.message.SnsMessage;
import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sns.message.SnsNotification;
import com.amazonaws.services.sns.message.SnsSubscriptionConfirmation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class S3WebhookMessageService {

  private final s3WebhookService s3WebhookService;
  private final SnsMessageManager snsMessageManager;

  public void handleMessage(String rawBody) {

    SnsMessage message =
        snsMessageManager.parseMessage(
            new ByteArrayInputStream(rawBody.getBytes(StandardCharsets.UTF_8)));

    if (message instanceof SnsSubscriptionConfirmation) {
      SnsSubscriptionConfirmation confirmMsg = (SnsSubscriptionConfirmation) message;
      String subscribeURL = confirmMsg.getSubscribeUrl().toString();
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(subscribeURL)).build();
      try {
        client.send(request, HttpResponse.BodyHandlers.discarding());
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    } else if (message instanceof SnsNotification) {
      if (message instanceof SnsNotification) {
        SnsNotification notification = (SnsNotification) message;
        String messageContent = notification.getMessage();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(messageContent);
        JsonNode records = root.get("Records");

        if (records != null && records.isArray()) {
          for (JsonNode record : records) {
            JsonNode s3 = record.get("s3");
            if (s3 != null) {
              String objectKey = s3.get("object").get("key").toString();
              s3WebhookService.processS3PutEvent(objectKey);
            }
          }
        }
      }
    }
  }
}
