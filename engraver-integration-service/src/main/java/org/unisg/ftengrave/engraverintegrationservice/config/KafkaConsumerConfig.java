package org.unisg.ftengrave.engraverintegrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.engraverintegrationservice.dto.EngraverCommandDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, EngraverCommandDto> consumerFactory() {
    return consumerFactory(EngraverCommandDto.class);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, EngraverCommandDto>
      kafkaListenerContainerFactory() {
    return kafkaListenerContainerFactory(consumerFactory());
  }
}
