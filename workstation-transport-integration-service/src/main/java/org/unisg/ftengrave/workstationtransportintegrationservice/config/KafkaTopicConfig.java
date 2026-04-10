package org.unisg.ftengrave.workstationtransportintegrationservice.config;

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

  @Value("${kafka.topic.workstation-transport-command}")
  private String workstationTransportCommandTopic;

  @Value("${kafka.topic.workstation-transport-event}")
  private String workstationTransportEventTopic;

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
  public NewTopic workstationTransportCommandTopic() {
    return new NewTopic(workstationTransportCommandTopic, defaultPartitions, replicationFactor);
  }

  @Bean
  public NewTopic workstationTransportEventTopic() {
    return new NewTopic(workstationTransportEventTopic, defaultPartitions, replicationFactor);
  }
}
