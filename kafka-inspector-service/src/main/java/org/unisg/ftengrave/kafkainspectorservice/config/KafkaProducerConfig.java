package org.unisg.ftengrave.kafkainspectorservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaProducerConfig;

@Configuration
public class KafkaProducerConfig extends AbstractKafkaProducerConfig {

  @Bean
  public ProducerFactory<String, JsonNode> producerFactory() {
    return super.producerFactory();
  }

  @Bean
  public KafkaTemplate<String, JsonNode> kafkaTemplate() {
    return super.kafkaTemplate(producerFactory());
  }
}
