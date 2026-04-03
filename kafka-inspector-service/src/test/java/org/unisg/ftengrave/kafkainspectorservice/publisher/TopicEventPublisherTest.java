package org.unisg.ftengrave.kafkainspectorservice.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TopicEventPublisherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void publishesToConfiguredTopic() throws Exception {
    @SuppressWarnings("unchecked")
    KafkaOperations<String, com.fasterxml.jackson.databind.JsonNode> kafkaOperations =
        mock(KafkaOperations.class);
    TopicEventPublisher publisher = new TopicEventPublisher(kafkaOperations, "publish-topic");

    publisher.publish("item-7", objectMapper.readTree("{\"event\":\"test\"}"));

    verify(kafkaOperations).send("publish-topic", "item-7", objectMapper.readTree("{\"event\":\"test\"}"));
  }
}
