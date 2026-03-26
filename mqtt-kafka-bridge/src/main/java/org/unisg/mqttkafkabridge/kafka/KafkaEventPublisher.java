package org.unisg.mqttkafkabridge.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

  private final DefaultKafkaProducerFactory<String, String> producerFactory;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${kafka.topic.sorting-machine}")
  private String topic;

  public KafkaEventPublisher(ObjectMapper objectMapper,
                             @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
    this.objectMapper = objectMapper;

    Map<String, Object> configuration = new HashMap<>();
    configuration.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configuration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configuration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    producerFactory = new DefaultKafkaProducerFactory<>(configuration);
    kafkaTemplate = new KafkaTemplate<>(producerFactory);
  }

  public void publish(Object payload) {
    try {
      kafkaTemplate.send(topic, objectMapper.writeValueAsString(payload));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize Kafka payload", e);
    }
  }

  @PreDestroy
  public void stop() {
    producerFactory.destroy();
  }
}