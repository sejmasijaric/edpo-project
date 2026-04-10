package org.unisg.ftengrave.intakeservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "kafka.topic.auto-create", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {

    @Value("${kafka.bootstrap-address}")
    private String bootstrapAddress;

    @Value("${kafka.topic.stage-orchestration}")
    private String stageOrchestrationTopic;

    @Value("${kafka.topic.machine-orchestration}")
    private String machineOrchestrationTopic;

    @Value("${kafka.topic.user-task-management}")
    private String userTaskManagementTopic;

    @Value("${kafka.topic.vacuum-gripper-command}")
    private String vacuumGripperCommandTopic;

    @Value("${kafka.topic.vacuum-gripper-event}")
    private String vacuumGripperEventTopic;

    @Value("${kafka.topic.engraver-event}")
    private String engraverEventTopic;

    @Value("${kafka.topic.replication-factor:3}")
    private short replicationFactor;

    @Value("${kafka.topic.default-partitions:1}")
    private int defaultPartitions;

    @Value("${kafka.topic.stage-orchestration-partitions:3}")
    private int stageOrchestrationPartitions;

    @Value("${kafka.topic.machine-orchestration-partitions:3}")
    private int machineOrchestrationPartitions;

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
    public NewTopic userTaskManagementTopic() {
        return new NewTopic(userTaskManagementTopic, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic vacuumGripperCommandTopic() {
        return new NewTopic(vacuumGripperCommandTopic, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic vacuumGripperEventTopic() {
        return new NewTopic(vacuumGripperEventTopic, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic engraverEventTopic() {
        return new NewTopic(engraverEventTopic, defaultPartitions, replicationFactor);
    }
}
