package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.QcOutcomeEventDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@EnableKafka
@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, QcOutcomeEventDto> qcOutcomeConsumerFactory() {
        return consumerFactory(QcOutcomeEventDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, QcOutcomeEventDto> qcOutcomeKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(qcOutcomeConsumerFactory());
    }
}
