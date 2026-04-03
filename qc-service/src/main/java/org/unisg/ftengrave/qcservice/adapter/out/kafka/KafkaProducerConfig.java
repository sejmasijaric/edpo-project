package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.QcOutcomeEventDto;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;

@Configuration
@EnableConfigurationProperties(SorterIntegrationProperties.class)
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-address}")
    private String bootstrapAddress;

    @Value("${kafka.trusted-packages}")
    private String trustedPackages;

    @Bean
    public ProducerFactory<String, SortingMachineCommandDto> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, SortingMachineCommandDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, QcOutcomeEventDto> qcOutcomeProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, QcOutcomeEventDto> qcOutcomeKafkaTemplate() {
        return new KafkaTemplate<>(qcOutcomeProducerFactory());
    }

    @Bean
    public StringJsonMessageConverter kafkaMessageConverter() {
        return new StringJsonMessageConverter();
    }
}
