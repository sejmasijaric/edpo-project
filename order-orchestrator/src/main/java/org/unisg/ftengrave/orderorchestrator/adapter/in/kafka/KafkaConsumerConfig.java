package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.ServiceOutcomeEventDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@EnableKafka
@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ServiceOutcomeEventDto> serviceOutcomeConsumerFactory() {
        return consumerFactory(ServiceOutcomeEventDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ServiceOutcomeEventDto> serviceOutcomeKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(serviceOutcomeConsumerFactory());
    }
}
