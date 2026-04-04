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

    @Value("${kafka.topic.vacuum-gripper}")
    private String vacuumGripperTopic;

    @Value("${kafka.topic.engraver}")
    private String engraverTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic stageOrchestrationTopic() {
        return new NewTopic(stageOrchestrationTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic vacuumGripperTopic() {
        return new NewTopic(vacuumGripperTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic engraverTopic() {
        return new NewTopic(engraverTopic, 1, (short) 1);
    }
}
