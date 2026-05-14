package org.unisg.ftengrave.dashboardservice.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.dashboardservice.dto.DashboardEvent;
import org.unisg.ftengrave.dashboardservice.dto.ItemState;

class DashboardTopologyTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final JsonSerde<DashboardEvent> dashboardEventSerde =
      new JsonSerde<>(objectMapper, DashboardEvent.class);

  @Test
  void streamStreamJoinCreatesManufacturingDurationEvent() {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    new DashboardTopology(
        objectMapper,
        "stage-orchestration",
        "machine-orchestration",
        "user-task-management",
        "order-created",
        "dashboard.metrics",
        Duration.ofHours(12))
        .dashboardStream(streamsBuilder);

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties())) {
      TestInputTopic<String, String> stageTopic = testDriver.createInputTopic(
          "stage-orchestration",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestInputTopic<String, String> machineTopic = testDriver.createInputTopic(
          "machine-orchestration",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestOutputTopic<String, DashboardEvent> dashboardTopic = testDriver.createOutputTopic(
          "dashboard.metrics",
          Serdes.String().deserializer(),
          dashboardEventSerde.deserializer());

      stageTopic.pipeInput("item-42", """
          {"commandType":"run-production-command","itemIdentifier":"item-42"}
          """, Instant.parse("2026-05-14T10:00:00Z"));
      machineTopic.pipeInput("item-42", """
          {"outcomeType":"manufacturing-completed","itemIdentifier":"item-42"}
          """, Instant.parse("2026-05-14T10:04:30Z"));

      List<KeyValue<String, DashboardEvent>> events = dashboardTopic.readKeyValuesToList();

      DashboardEvent durationEvent = events.stream()
          .map(event -> event.value)
          .filter(event -> DashboardEventNormalizer.MANUFACTURING_ATTEMPT_DURATION.equals(event.eventType()))
          .findFirst()
          .orElseThrow();
      assertEquals("item-42", durationEvent.itemIdentifier());
      assertEquals(270_000L, durationEvent.durationMillis());
      assertEquals("manufacturing-completed", durationEvent.attributes().get("outcomeType"));
    }
  }

  @Test
  void stateStoreTracksWipTerminalItemsManualInterventionsAndRetries() throws Exception {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    new DashboardTopology(
        objectMapper,
        "stage-orchestration",
        "machine-orchestration",
        "user-task-management",
        "order-created",
        "dashboard.metrics",
        Duration.ofHours(12))
        .dashboardStream(streamsBuilder);

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties())) {
      TestInputTopic<String, String> stageTopic = testDriver.createInputTopic(
          "stage-orchestration",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestInputTopic<String, String> machineTopic = testDriver.createInputTopic(
          "machine-orchestration",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestInputTopic<String, String> userTaskTopic = testDriver.createInputTopic(
          "user-task-management",
          Serdes.String().serializer(),
          Serdes.String().serializer());

      stageTopic.pipeInput("item-1", """
          {"commandType":"run-item-intake-command","itemIdentifier":"item-1"}
          """, Instant.parse("2026-05-14T09:00:00Z"));
      stageTopic.pipeInput("item-1", """
          {"commandType":"run-item-intake-command","itemIdentifier":"item-1"}
          """, Instant.parse("2026-05-14T09:01:00Z"));
      userTaskTopic.pipeInput("item-1", """
          {"taskName":"Resolve Issue and Replace Item","taskCategory":"intake","stationName":"Intake"}
          """, Instant.parse("2026-05-14T09:02:00Z"));
      machineTopic.pipeInput("item-1", """
          {"outcomeType":"qc-rejection","itemIdentifier":"item-1"}
          """, Instant.parse("2026-05-14T09:10:00Z"));

      KeyValueStore<String, String> itemsStore =
          testDriver.getKeyValueStore(DashboardStateTransformer.DASHBOARD_ITEMS_STORE);
      ItemState itemState = objectMapper.readValue(itemsStore.get("item-1"), ItemState.class);

      assertTrue(itemState.isTerminal());
      assertEquals("qc-rejection", itemState.getTerminalOutcome());
      assertEquals(1, itemState.getRetryCount());
      assertEquals(2, itemState.getAttemptCounts().get("run-item-intake-command"));
    }
  }

  private Properties properties() {
    Properties properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "dashboard-service-test");
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
    return properties;
  }
}
