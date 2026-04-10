package org.unisg.ftengrave.sharedkafka.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

public abstract class AbstractKafkaConsumerConfig {

  @Value("${kafka.bootstrap-address}")
  private String bootstrapAddress;

  @Value("${kafka.group-id}")
  private String groupId;

  @Value("${kafka.trusted-packages}")
  private String trustedPackages;

  @Value("${spring.kafka.listener.auto-startup:true}")
  private boolean autoStartup;

  @Value("${kafka.consumer.auto-offset-reset:latest}")
  private String autoOffsetReset;

  @Value("${kafka.consumer.max-poll-records:50}")
  private int maxPollRecords;

  @Value("${kafka.consumer.concurrency:1}")
  private int concurrency;

  @Value("${kafka.consumer.retry-interval-ms:1000}")
  private long retryIntervalMs;

  @Value("${kafka.consumer.retry-max-attempts:9}")
  private long retryMaxAttempts;

  protected <V> ConsumerFactory<String, V> consumerFactory(Class<V> valueType) {
    return new DefaultKafkaConsumerFactory<>(
        consumerProperties(),
        new StringDeserializer(),
        new JsonDeserializer<>(valueType, false));
  }

  protected <V> ConcurrentKafkaListenerContainerFactory<String, V> kafkaListenerContainerFactory(
      ConsumerFactory<String, V> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, V> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setAutoStartup(autoStartup);
    factory.setConcurrency(concurrency);
    factory.setCommonErrorHandler(
        new DefaultErrorHandler(new FixedBackOff(retryIntervalMs, retryMaxAttempts)));
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
    factory.getContainerProperties().setSyncCommits(true);
    factory.getContainerProperties().setDeliveryAttemptHeader(true);
    return factory;
  }

  private Map<String, Object> consumerProperties() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
    return props;
  }
}
