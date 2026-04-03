package org.unisg.ftengrave.polishingmachineintegrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.polishingmachineintegrationservice.dto.PolishingMachineCommandDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, PolishingMachineCommandDto> consumerFactory() {
    return consumerFactory(PolishingMachineCommandDto.class);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, PolishingMachineCommandDto>
      kafkaListenerContainerFactory() {
    return kafkaListenerContainerFactory(consumerFactory());
  }
}
