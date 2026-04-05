package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.EngraverCommandDto;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.ManufacturingOutcomeEventDto;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.PolishingMachineCommandDto;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.WorkstationTransportCommandDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaProducerConfig;

@Configuration
@EnableConfigurationProperties({
        EngraverIntegrationProperties.class,
        WorkstationTransportIntegrationProperties.class,
        PolishingMachineIntegrationProperties.class
})
public class KafkaProducerConfig extends AbstractKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EngraverCommandDto> engraverProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, EngraverCommandDto> engraverKafkaTemplate() {
        return kafkaTemplate(engraverProducerFactory());
    }

    @Bean
    public ProducerFactory<String, WorkstationTransportCommandDto> workstationTransportProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, WorkstationTransportCommandDto> workstationTransportKafkaTemplate() {
        return kafkaTemplate(workstationTransportProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PolishingMachineCommandDto> polishingMachineProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, PolishingMachineCommandDto> polishingMachineKafkaTemplate() {
        return kafkaTemplate(polishingMachineProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ManufacturingOutcomeEventDto> manufacturingOutcomeProducerFactory() {
        return super.producerFactory();
    }

    @Bean
    public KafkaTemplate<String, ManufacturingOutcomeEventDto> manufacturingOutcomeKafkaTemplate() {
        return kafkaTemplate(manufacturingOutcomeProducerFactory());
    }
}
