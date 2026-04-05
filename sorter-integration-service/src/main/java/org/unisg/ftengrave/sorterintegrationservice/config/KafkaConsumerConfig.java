package org.unisg.ftengrave.sorterintegrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineCommandDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, SortingMachineCommandDto> consumerFactory() {
    return consumerFactory(SortingMachineCommandDto.class);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SortingMachineCommandDto>
      kafkaListenerContainerFactory() {
    return kafkaListenerContainerFactory(consumerFactory());
  }
}
