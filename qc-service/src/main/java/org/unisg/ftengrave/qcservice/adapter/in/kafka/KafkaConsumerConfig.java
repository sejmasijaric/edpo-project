package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.bootstrap-address}")
    private String bootstrapAddress;

    @Value("${kafka.group-id}")
    private String groupId;

    @Value("${kafka.trusted-packages}")
    private String trustedPackages;

    @Value("${spring.kafka.listener.auto-startup:true}")
    private boolean autoStartup;

    @Bean
    public ConsumerFactory<String, SortingMachineEventDto> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(SortingMachineEventDto.class, false)
        );
    }

    @Bean
    public ConsumerFactory<String, PerformQcCommandDto> performQcCommandConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(PerformQcCommandDto.class, false)
        );
    }

    private Map<String, Object> consumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SortingMachineEventDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SortingMachineEventDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PerformQcCommandDto>
    performQcCommandKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PerformQcCommandDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(performQcCommandConsumerFactory());
        factory.setAutoStartup(autoStartup);
        return factory;
    }
}
