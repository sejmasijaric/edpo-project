package com.example.frontendspringbootservice.config;

import com.example.frontendspringbootservice.dto.LatestItemStatus;
import com.example.frontendspringbootservice.dto.MachineOrchestrationEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
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
import java.util.UUID;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    private final ObjectProvider<KafkaConnectionDetails> connectionDetailsProvider;

    @Value("${app.kafka.consumer.group-id:frontend-service-group}")
    private String groupId;

    public KafkaConsumerConfig(
            KafkaProperties kafkaProperties,
            ObjectProvider<KafkaConnectionDetails> connectionDetailsProvider) {
        this.kafkaProperties = kafkaProperties;
        this.connectionDetailsProvider = connectionDetailsProvider;
    }

    /**
     * Build the base consumer properties from Spring Boot's autoconfigured
     * KafkaProperties, then overlay the bootstrap servers from
     * KafkaConnectionDetails when present. This keeps the production config
     * file authoritative while letting testcontainers' @ServiceConnection
     * redirect consumers to the ephemeral test broker.
     */
    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        KafkaConnectionDetails details = connectionDetailsProvider.getIfAvailable();
        if (details != null) {
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, details.getBootstrapServers());
        }
        return props;
    }

    @Bean
    public ConsumerFactory<String, MachineOrchestrationEvent> machineOrchestrationConsumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = baseConsumerProps();
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

    @Bean
    public ConsumerFactory<String, LatestItemStatus> latestItemStatusConsumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                groupId + "-latest-status-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, LatestItemStatus> factory =
                new DefaultKafkaConsumerFactory<>(props);
        factory.setValueDeserializer(new Jackson3Deserializer<>(objectMapper, LatestItemStatus.class));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LatestItemStatus> latestItemStatusListenerContainerFactory(
            ConsumerFactory<String, LatestItemStatus> latestItemStatusConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, LatestItemStatus> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(latestItemStatusConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> userTaskEventConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        // Per-instance group id forces a from-the-beginning replay on every boot so
        // the in-memory open-tasks state is always rebuilt from the full topic
        // history. Cheap because the topic is low-volume.
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                groupId + "-user-tasks-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> userTaskEventListenerContainerFactory(
            ConsumerFactory<String, String> userTaskEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userTaskEventConsumerFactory);
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
