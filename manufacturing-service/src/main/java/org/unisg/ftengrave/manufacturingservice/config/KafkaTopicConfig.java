package org.unisg.ftengrave.manufacturingservice.config;

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

    @Value("${kafka.topic.engraver}")
    private String engraverTopic;

    @Value("${kafka.topic.workstation-transport}")
    private String workstationTransportTopic;

    @Value("${kafka.topic.polishing-machine}")
    private String polishingMachineTopic;

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
    public NewTopic engraverTopic() {
        return new NewTopic(engraverTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic workstationTransportTopic() {
        return new NewTopic(workstationTransportTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic polishingMachineTopic() {
        return new NewTopic(polishingMachineTopic, 1, (short) 1);
    }
}
