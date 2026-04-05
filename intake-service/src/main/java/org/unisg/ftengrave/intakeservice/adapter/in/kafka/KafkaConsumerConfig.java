package org.unisg.ftengrave.intakeservice.adapter.in.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.EngraverEventDto;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.RunItemIntakeCommandDto;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.VacuumGripperEventDto;
import org.unisg.ftengrave.sharedkafka.config.AbstractKafkaConsumerConfig;

@EnableKafka
@Configuration
public class KafkaConsumerConfig extends AbstractKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, VacuumGripperEventDto> consumerFactory() {
        return consumerFactory(VacuumGripperEventDto.class);
    }

    @Bean
    public ConsumerFactory<String, RunItemIntakeCommandDto> intakeCommandConsumerFactory() {
        return consumerFactory(RunItemIntakeCommandDto.class);
    }

    @Bean
    public ConsumerFactory<String, EngraverEventDto> engraverEventConsumerFactory() {
        return consumerFactory(EngraverEventDto.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VacuumGripperEventDto> kafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(consumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RunItemIntakeCommandDto>
    intakeCommandKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(intakeCommandConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EngraverEventDto>
    engraverEventKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory(engraverEventConsumerFactory());
    }
}
