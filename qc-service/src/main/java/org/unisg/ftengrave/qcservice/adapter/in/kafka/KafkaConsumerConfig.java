package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@EnableKafka
@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, SortingMachineEventDto> consumerFactory() {
        return consumerFactory(SortingMachineEventDto.class);
    }

    @Bean
    public ConsumerFactory<String, PerformQcCommandDto> performQcCommandConsumerFactory() {
        return consumerFactory(PerformQcCommandDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SortingMachineEventDto> kafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(consumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PerformQcCommandDto>
    performQcCommandKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(performQcCommandConsumerFactory());
    }
}
