package org.unisg.ftengrave.kafkainspectorservice.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class TopicEventPublisher extends TransactionAwareKafkaPublisher<String, JsonNode>
    implements PublishTopicEventUseCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicEventPublisher.class);

  private final String publishTopic;

  public TopicEventPublisher(
      KafkaOperations<String, JsonNode> kafkaOperations,
      @Value("${kafka.topic.publish}") String publishTopic) {
    super(kafkaOperations);
    this.publishTopic = publishTopic;
  }

  @Override
  public void publish(String key, JsonNode payload) {
    publishAfterCommitOrNow(() -> send(publishTopic, key, payload));
    LOGGER.info("Published Kafka event to topic '{}' with key '{}': {}", publishTopic, key, payload);
  }
}
