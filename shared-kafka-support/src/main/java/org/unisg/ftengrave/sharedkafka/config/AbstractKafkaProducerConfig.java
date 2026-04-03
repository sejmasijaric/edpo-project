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

  protected <V> ProducerFactory<String, V> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
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
