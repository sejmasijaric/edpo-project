package org.unisg.ftengrave.kafkainspectorservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class TopicEventConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicEventConsumer.class);

  @KafkaListener(topics = "${kafka.topic.subscribe}")
  public void receive(
      JsonNode payload,
      @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    LOGGER.info("Received Kafka event on topic '{}' with key '{}': {}", topic, key, payload);
  }
}
