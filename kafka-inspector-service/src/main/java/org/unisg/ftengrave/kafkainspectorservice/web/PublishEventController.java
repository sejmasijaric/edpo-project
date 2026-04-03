package org.unisg.ftengrave.kafkainspectorservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.kafkainspectorservice.publisher.PublishTopicEventUseCase;

@RestController
@RequestMapping("/api/kafka/messages")
public class PublishEventController {

  private final PublishTopicEventUseCase topicEventPublisher;

  public PublishEventController(PublishTopicEventUseCase topicEventPublisher) {
    this.topicEventPublisher = topicEventPublisher;
  }

  @PostMapping
  public ResponseEntity<Void> publish(@RequestBody PublishEventRequest request) {
    topicEventPublisher.publish(request.key(), request.payload());
    return ResponseEntity.accepted().build();
  }

  public record PublishEventRequest(String key, JsonNode payload) {
  }
}
