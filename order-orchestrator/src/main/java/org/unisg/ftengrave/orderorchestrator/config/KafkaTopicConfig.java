package org.unisg.ftengrave.orderorchestrator.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(name = "kafka.topic.auto-create", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {

    @Value("${kafka.bootstrap-address}")
    private String bootstrapAddress;

    @Value("${kafka.topic.stage-orchestration}")
    private String stageOrchestrationTopic;

    @Value("${kafka.topic.machine-orchestration}")
    private String machineOrchestrationTopic;

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.replication-factor:3}")
    private short replicationFactor;

    @Value("${kafka.topic.stage-orchestration-partitions:3}")
    private int stageOrchestrationPartitions;

    @Value("${kafka.topic.machine-orchestration-partitions:3}")
    private int machineOrchestrationPartitions;

    @Value("${kafka.topic.order-created-partitions:3}")
    private int orderCreatedPartitions;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic stageOrchestrationTopic() {
        return new NewTopic(stageOrchestrationTopic, stageOrchestrationPartitions, replicationFactor);
    }

    @Bean
    public NewTopic machineOrchestrationTopic() {
        return new NewTopic(machineOrchestrationTopic, machineOrchestrationPartitions, replicationFactor);
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic(orderCreatedTopic, orderCreatedPartitions, replicationFactor);
    }
}
