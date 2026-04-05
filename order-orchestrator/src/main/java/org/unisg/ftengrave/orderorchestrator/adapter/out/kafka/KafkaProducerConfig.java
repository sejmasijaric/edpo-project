package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.RunItemIntakeCommandDto;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.RunProductionCommandDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaProducerConfig;

@Configuration
public class KafkaProducerConfig extends AbstractKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, PerformQcCommandDto> producerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, PerformQcCommandDto> kafkaTemplate() {
        return kafkaTemplate(producerFactory());
    }

    @Bean
    public ProducerFactory<String, RunItemIntakeCommandDto> runItemIntakeCommandProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, RunItemIntakeCommandDto> runItemIntakeCommandKafkaTemplate() {
        return kafkaTemplate(runItemIntakeCommandProducerFactory());
    }

    @Bean
    public ProducerFactory<String, RunProductionCommandDto> runProductionCommandProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, RunProductionCommandDto> runProductionCommandKafkaTemplate() {
        return kafkaTemplate(runProductionCommandProducerFactory());
    }
}
