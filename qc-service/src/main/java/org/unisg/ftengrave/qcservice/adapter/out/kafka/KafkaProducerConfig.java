package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.QcOutcomeEventDto;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaProducerConfig;

@Configuration
@EnableConfigurationProperties(SorterIntegrationProperties.class)
public class KafkaProducerConfig extends AbstractKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, SortingMachineCommandDto> producerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, SortingMachineCommandDto> kafkaTemplate() {
        return kafkaTemplate(producerFactory());
    }

    @Bean
    public ProducerFactory<String, QcOutcomeEventDto> qcOutcomeProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, QcOutcomeEventDto> qcOutcomeKafkaTemplate() {
        return kafkaTemplate(qcOutcomeProducerFactory());
    }
}
