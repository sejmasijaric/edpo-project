package com.example.frontendspringbootservice.config;

import com.example.frontendspringbootservice.dto.MachineOrchestrationEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.consumer.group-id:frontend-service-group}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, MachineOrchestrationEvent> machineOrchestrationConsumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, MachineOrchestrationEvent> factory =
                new DefaultKafkaConsumerFactory<>(props);
        factory.setValueDeserializer(new Jackson3Deserializer<>(objectMapper, MachineOrchestrationEvent.class));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MachineOrchestrationEvent> machineOrchestrationListenerContainerFactory(
            ConsumerFactory<String, MachineOrchestrationEvent> machineOrchestrationConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, MachineOrchestrationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(machineOrchestrationConsumerFactory);
        return factory;
    }

    static class Jackson3Deserializer<T> implements Deserializer<T> {
        private final ObjectMapper objectMapper;
        private final Class<T> targetType;

        Jackson3Deserializer(ObjectMapper objectMapper, Class<T> targetType) {
            this.objectMapper = objectMapper;
            this.targetType = targetType;
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {}

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) return null;
            return objectMapper.readValue(new String(data, StandardCharsets.UTF_8), targetType);
        }

        @Override
        public void close() {}
    }
}
