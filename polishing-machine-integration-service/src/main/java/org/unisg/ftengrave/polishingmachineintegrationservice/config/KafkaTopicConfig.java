package org.unisg.ftengrave.polishingmachineintegrationservice.config;

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

  @Value("${kafka.topic.polishing-machine-command}")
  private String polishingMachineCommandTopic;

  @Value("${kafka.topic.polishing-machine-event}")
  private String polishingMachineEventTopic;

  @Value("${kafka.topic.replication-factor:3}")
  private short replicationFactor;

  @Value("${kafka.topic.default-partitions:1}")
  private int defaultPartitions;

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic polishingMachineCommandTopic() {
    return new NewTopic(polishingMachineCommandTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic polishingMachineEventTopic() {
    return new NewTopic(polishingMachineEventTopic, defaultPartitions, replicationFactor);
  }
}
