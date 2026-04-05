package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.EngraverEventDto;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.PolishingMachineEventDto;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.RunProductionCommandDto;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.WorkstationTransportEventDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@EnableKafka
@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, RunProductionCommandDto> runProductionCommandConsumerFactory() {
        return consumerFactory(RunProductionCommandDto.class);
    }

    @Bean
    public ConsumerFactory<String, EngraverEventDto> engraverEventConsumerFactory() {
        return consumerFactory(EngraverEventDto.class);
    }

    @Bean
    public ConsumerFactory<String, WorkstationTransportEventDto> workstationTransportEventConsumerFactory() {
        return consumerFactory(WorkstationTransportEventDto.class);
    }

    @Bean
    public ConsumerFactory<String, PolishingMachineEventDto> polishingMachineEventConsumerFactory() {
        return consumerFactory(PolishingMachineEventDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RunProductionCommandDto>
    runProductionCommandKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(runProductionCommandConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EngraverEventDto>
    engraverEventKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(engraverEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WorkstationTransportEventDto>
    workstationTransportEventKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(workstationTransportEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PolishingMachineEventDto>
    polishingMachineEventKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(polishingMachineEventConsumerFactory());
    }
}
