package org.unisg.ftengrave.factoryeventstreams.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.config.TopicConfig;
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

  @Value("${kafka.topic.raw-factory-event}")
  private String rawFactoryEventTopic;

  @Value("${kafka.topic.sorting-machine-event}")
  private String sortingMachineEventTopic;

  @Value("${kafka.topic.vacuum-gripper-event}")
  private String vacuumGripperEventTopic;

  @Value("${kafka.topic.engraver-event}")
  private String engraverEventTopic;

  @Value("${kafka.topic.polishing-machine-event}")
  private String polishingMachineEventTopic;

  @Value("${kafka.topic.workstation-transport-event}")
  private String workstationTransportEventTopic;

  @Value("${kafka.topic.latest-item-status}")
  private String latestItemStatusTopic;

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
  public NewTopic rawFactoryEventTopic() {
    return new NewTopic(rawFactoryEventTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic sortingMachineEventTopic() {
    return new NewTopic(sortingMachineEventTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic vacuumGripperEventTopic() {
    return new NewTopic(vacuumGripperEventTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic engraverEventTopic() {
    return new NewTopic(engraverEventTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic polishingMachineEventTopic() {
    return new NewTopic(polishingMachineEventTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic workstationTransportEventTopic() {
    return new NewTopic(workstationTransportEventTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic latestItemStatusTopic() {
    return new NewTopic(latestItemStatusTopic, defaultPartitions, replicationFactor)
        .configs(Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));
  }
}
