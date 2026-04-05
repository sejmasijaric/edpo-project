package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.IntakeOutcomeEventDto;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.InsertItemIntoIntakeCommandDto;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.VacuumGripperCommandDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaProducerConfig;

@Configuration
@EnableConfigurationProperties(VacuumGripperIntegrationProperties.class)
public class KafkaProducerConfig extends AbstractKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, VacuumGripperCommandDto> producerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, VacuumGripperCommandDto> kafkaTemplate() {
        return kafkaTemplate(producerFactory());
    }

    @Bean
    public ProducerFactory<String, IntakeOutcomeEventDto> intakeOutcomeProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, IntakeOutcomeEventDto> intakeOutcomeKafkaTemplate() {
        return kafkaTemplate(intakeOutcomeProducerFactory());
    }

    @Bean
    public ProducerFactory<String, InsertItemIntoIntakeCommandDto> userTaskManagementProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, InsertItemIntoIntakeCommandDto> userTaskManagementKafkaTemplate() {
        return kafkaTemplate(userTaskManagementProducerFactory());
    }
}
