package org.unisg.ftengrave.workstationtransportintegrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;
import org.unisg.ftengrave.workstationtransportintegrationservice.dto.WorkstationTransportCommandDto;

@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, WorkstationTransportCommandDto> consumerFactory() {
    return consumerFactory(WorkstationTransportCommandDto.class);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, WorkstationTransportCommandDto>
      kafkaListenerContainerFactory() {
    return kafkaListenerContainerFactory(consumerFactory());
  }
}
