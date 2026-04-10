package com.example.frontendspringbootservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.order-events:order-events}")
    private String orderEventsTopic;

    @Value("${app.kafka.topic.order-created:order-created}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topic.replication-factor:3}")
    private short replicationFactor;

    @Value("${app.kafka.topic.order-events-partitions:3}")
    private int orderEventsPartitions;

    @Value("${app.kafka.topic.order-created-partitions:3}")
    private int orderCreatedPartitions;

    @Bean
    public NewTopic orderEventsTopic() {
        return new NewTopic(orderEventsTopic, orderEventsPartitions, replicationFactor);
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic(orderCreatedTopic, orderCreatedPartitions, replicationFactor);
    }
}
