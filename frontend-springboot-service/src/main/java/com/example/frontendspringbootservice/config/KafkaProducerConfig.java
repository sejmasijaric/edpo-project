package com.example.frontendspringbootservice.config;

import org.apache.kafka.common.serialization.Serializer;
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public DefaultKafkaProducerFactoryCustomizer kafkaProducerFactoryCustomizer(ObjectMapper objectMapper) {
        return factory -> factory.setValueSerializer(new Jackson3Serializer<>(objectMapper));
    }

    static class Jackson3Serializer<T> implements Serializer<T> {
        private final ObjectMapper objectMapper;

        Jackson3Serializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {}

        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) return null;
            return objectMapper.writeValueAsBytes(data);
        }

        @Override
        public void close() {}
    }
}
