package org.unisg.ftengrave.dashboardservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "kafka.topic.auto-create", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {

  @Value("${kafka.topic.dashboard-metrics}")
  private String dashboardMetricsTopic;

  @Value("${kafka.topic.stage-orchestration}")
  private String stageOrchestrationTopic;

  @Value("${kafka.topic.machine-orchestration}")
  private String machineOrchestrationTopic;

  @Value("${kafka.topic.user-task-management}")
  private String userTaskManagementTopic;

  @Value("${kafka.topic.order-created}")
  private String orderCreatedTopic;

  @Value("${kafka.topic.replication-factor:3}")
  private short replicationFactor;

  @Value("${kafka.topic.default-partitions:1}")
  private int defaultPartitions;

  @Value("${kafka.topic.stage-orchestration-partitions:3}")
  private int stageOrchestrationPartitions;

  @Value("${kafka.topic.machine-orchestration-partitions:3}")
  private int machineOrchestrationPartitions;

  @Value("${kafka.topic.order-created-partitions:3}")
  private int orderCreatedPartitions;

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
  public NewTopic orderCreatedTopic() {
    return new NewTopic(orderCreatedTopic, orderCreatedPartitions, replicationFactor);
  }

  @Bean
  public NewTopic dashboardMetricsTopic() {
    return new NewTopic(dashboardMetricsTopic, defaultPartitions, replicationFactor);
  }
}
