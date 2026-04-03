package org.unisg.ftengrave.sharedkafka.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public abstract class AbstractKafkaConsumerConfig {

  @Value("${kafka.bootstrap-address}")
  private String bootstrapAddress;

  @Value("${kafka.group-id}")
  private String groupId;

  @Value("${kafka.trusted-packages}")
  private String trustedPackages;

  @Value("${spring.kafka.listener.auto-startup:true}")
  private boolean autoStartup;

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
    return factory;
  }

  private Map<String, Object> consumerProperties() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
    return props;
  }
}
