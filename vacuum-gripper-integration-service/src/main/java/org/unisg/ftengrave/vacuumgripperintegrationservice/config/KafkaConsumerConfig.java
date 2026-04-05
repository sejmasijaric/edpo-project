package org.unisg.ftengrave.vacuumgripperintegrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;
import org.unisg.ftengrave.vacuumgripperintegrationservice.dto.VacuumGripperCommandDto;

@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, VacuumGripperCommandDto> consumerFactory() {
    return consumerFactory(VacuumGripperCommandDto.class);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, VacuumGripperCommandDto>
      kafkaListenerContainerFactory() {
    return kafkaListenerContainerFactory(consumerFactory());
  }
}
