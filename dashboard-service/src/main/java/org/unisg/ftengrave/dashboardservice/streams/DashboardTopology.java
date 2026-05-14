package org.unisg.ftengrave.dashboardservice.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.dashboardservice.dto.DashboardEvent;
import org.unisg.ftengrave.dashboardservice.dto.ManufacturingOutcome;
import org.unisg.ftengrave.dashboardservice.dto.ManufacturingStart;

@Component
public class DashboardTopology {

  private final ObjectMapper objectMapper;
  private final String stageOrchestrationTopic;
  private final String machineOrchestrationTopic;
  private final String userTaskManagementTopic;
  private final String orderCreatedTopic;
  private final String dashboardMetricsTopic;
  private final Duration manufacturingJoinWindow;

  public DashboardTopology(
      ObjectMapper objectMapper,
      @Value("${kafka.topic.stage-orchestration}") String stageOrchestrationTopic,
      @Value("${kafka.topic.machine-orchestration}") String machineOrchestrationTopic,
      @Value("${kafka.topic.user-task-management}") String userTaskManagementTopic,
      @Value("${kafka.topic.order-created}") String orderCreatedTopic,
      @Value("${kafka.topic.dashboard-metrics}") String dashboardMetricsTopic,
      @Value("${kafka.streams.manufacturing-join-window}") Duration manufacturingJoinWindow) {
    this.objectMapper = objectMapper;
    this.stageOrchestrationTopic = stageOrchestrationTopic;
    this.machineOrchestrationTopic = machineOrchestrationTopic;
    this.userTaskManagementTopic = userTaskManagementTopic;
    this.orderCreatedTopic = orderCreatedTopic;
    this.dashboardMetricsTopic = dashboardMetricsTopic;
    this.manufacturingJoinWindow = manufacturingJoinWindow;
  }

  @Bean
  public KStream<String, DashboardEvent> dashboardStream(StreamsBuilder streamsBuilder) {
    streamsBuilder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(DashboardStateTransformer.DASHBOARD_EVENTS_STORE),
        Serdes.String(),
        Serdes.String()));
    streamsBuilder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(DashboardStateTransformer.DASHBOARD_ITEMS_STORE),
        Serdes.String(),
        Serdes.String()));

    DashboardEventNormalizer normalizer = new DashboardEventNormalizer(objectMapper);
    JsonSerde<DashboardEvent> dashboardEventSerde = new JsonSerde<>(objectMapper, DashboardEvent.class);
    JsonSerde<ManufacturingStart> manufacturingStartSerde =
        new JsonSerde<>(objectMapper, ManufacturingStart.class);
    JsonSerde<ManufacturingOutcome> manufacturingOutcomeSerde =
        new JsonSerde<>(objectMapper, ManufacturingOutcome.class);

    KStream<String, DashboardEvent> stageEvents = normalizedStream(
        streamsBuilder, stageOrchestrationTopic, normalizer);
    KStream<String, DashboardEvent> machineEvents = normalizedStream(
        streamsBuilder, machineOrchestrationTopic, normalizer);
    KStream<String, DashboardEvent> userTaskEvents = normalizedStream(
        streamsBuilder, userTaskManagementTopic, normalizer);
    KStream<String, DashboardEvent> orderEvents = normalizedStream(
        streamsBuilder, orderCreatedTopic, normalizer);

    KStream<String, ManufacturingStart> manufacturingStarts = stageEvents
        .filter((key, event) -> DashboardEventNormalizer.RUN_PRODUCTION_COMMAND.equals(event.eventType()))
        .selectKey((key, event) -> event.itemIdentifier())
        .mapValues(event -> new ManufacturingStart(event.itemIdentifier(), event.timestamp()));

    KStream<String, ManufacturingOutcome> manufacturingOutcomes = machineEvents
        .filter((key, event) -> DashboardEventNormalizer.MANUFACTURING_COMPLETED.equals(event.eventType())
            || DashboardEventNormalizer.MANUFACTURING_FAILED.equals(event.eventType()))
        .selectKey((key, event) -> event.itemIdentifier())
        .mapValues(event -> new ManufacturingOutcome(event.itemIdentifier(), event.eventType(), event.timestamp()));

    KStream<String, DashboardEvent> manufacturingDurations = manufacturingStarts.join(
        manufacturingOutcomes,
        (start, outcome) -> normalizer.manufacturingAttemptDuration(
            "stream-stream-join:" + stageOrchestrationTopic + "+" + machineOrchestrationTopic,
            start.itemIdentifier(),
            outcome.outcomeType(),
            start.timestamp(),
            outcome.timestamp(),
            Math.max(0, outcome.timestamp() - start.timestamp())),
        JoinWindows.ofTimeDifferenceWithNoGrace(manufacturingJoinWindow),
        StreamJoined.with(Serdes.String(), manufacturingStartSerde, manufacturingOutcomeSerde));

    KStream<String, DashboardEvent> allDashboardEvents =
        stageEvents.merge(machineEvents).merge(userTaskEvents).merge(orderEvents).merge(manufacturingDurations)
            .selectKey((key, event) -> event.eventId());

    allDashboardEvents
        .transformValues(() -> new DashboardStateTransformer(objectMapper),
            DashboardStateTransformer.DASHBOARD_EVENTS_STORE,
            DashboardStateTransformer.DASHBOARD_ITEMS_STORE)
        .to(dashboardMetricsTopic, Produced.with(Serdes.String(), dashboardEventSerde));

    return allDashboardEvents;
  }

  private KStream<String, DashboardEvent> normalizedStream(
      StreamsBuilder streamsBuilder,
      String topic,
      DashboardEventNormalizer normalizer) {
    return streamsBuilder.stream(topic, Consumed.with(Serdes.String(), Serdes.String()))
        .transform(() -> new NormalizingTransformer(topic, normalizer));
  }
}
