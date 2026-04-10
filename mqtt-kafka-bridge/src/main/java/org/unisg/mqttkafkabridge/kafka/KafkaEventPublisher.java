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

  @Value("${kafka.topic.bridge-target:${kafka.topic.sorting-machine-event}}")
  private String topic;

  @Value("${kafka.producer.acks:all}")
  private String acks;

  @Value("${kafka.producer.enable-idempotence:true}")
  private boolean enableIdempotence;

  @Value("${kafka.producer.retries:10}")
  private int retries;

  @Value("${kafka.producer.retry-backoff-ms:1000}")
  private int retryBackoffMs;

  @Value("${kafka.producer.delivery-timeout-ms:120000}")
  private int deliveryTimeoutMs;

  @Value("${kafka.producer.request-timeout-ms:30000}")
  private int requestTimeoutMs;

  @Value("${kafka.producer.max-in-flight-requests-per-connection:5}")
  private int maxInFlightRequestsPerConnection;

  @Value("${kafka.producer.compression-type:snappy}")
  private String compressionType;

  @Value("${kafka.producer.linger-ms:5}")
  private int lingerMs;

  @Value("${kafka.producer.batch-size:32768}")
  private int batchSize;

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

  @jakarta.annotation.PostConstruct
  void configureProducerDefaults() {
    Map<String, Object> updates = new HashMap<>();
    updates.put(ProducerConfig.ACKS_CONFIG, acks);
    updates.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
    updates.put(ProducerConfig.RETRIES_CONFIG, retries);
    updates.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
    updates.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
    updates.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
    updates.put(
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
        maxInFlightRequestsPerConnection);
    updates.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
    updates.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
    updates.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
    producerFactory.updateConfigs(updates);
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
