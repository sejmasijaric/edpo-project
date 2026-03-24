package org.example.mqttkafkabridge.kafka;

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

  @Value("${kafka.topic}")
  private String topic;

  public KafkaEventPublisher() {
    Map<String, Object> configuration = new HashMap<>();
    configuration.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    configuration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configuration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    producerFactory = new DefaultKafkaProducerFactory<>(configuration);
    kafkaTemplate = new KafkaTemplate<>(producerFactory);
  }

  public void publish(String payload) {
    kafkaTemplate.send(topic, payload);
  }

  @PreDestroy
  public void stop() {
    producerFactory.destroy();
  }
}
