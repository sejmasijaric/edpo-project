package org.unisg.ftengrave.sharedkafka.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public abstract class AbstractKafkaProducerConfig {

  @Value("${kafka.bootstrap-address}")
  private String bootstrapAddress;

  @Value("${kafka.trusted-packages}")
  private String trustedPackages;

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

  protected <V> ProducerFactory<String, V> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(ProducerConfig.ACKS_CONFIG, acks);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
    props.put(ProducerConfig.RETRIES_CONFIG, retries);
    props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
    props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
    props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
    props.put(
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
        maxInFlightRequestsPerConnection);
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
    props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
    return new DefaultKafkaProducerFactory<>(props);
  }

  protected <V> KafkaTemplate<String, V> kafkaTemplate(ProducerFactory<String, V> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public StringJsonMessageConverter kafkaMessageConverter() {
    return new StringJsonMessageConverter();
  }
}
