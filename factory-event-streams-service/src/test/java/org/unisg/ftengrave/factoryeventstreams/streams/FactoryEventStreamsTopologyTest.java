package org.unisg.ftengrave.factoryeventstreams.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.LatestItemStatus;
import org.unisg.ftengrave.factoryeventstreams.serialization.LatestItemStatusDeserializer;
import org.unisg.ftengrave.factoryeventstreams.translation.sorting.SortingMachineSensorTranslator;
import org.unisg.ftengrave.factoryeventstreams.translation.vacuum.VacuumGripperSensorTranslator;

class FactoryEventStreamsTopologyTest {

  @Test
  void routesRawSortingMachineEventToExistingSortingMachineEventTopic() {
    ObjectMapper objectMapper = new ObjectMapper();
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    SortingMachineSensorTranslator sortingTranslator =
        new SortingMachineSensorTranslator(objectMapper, "FTFactory/SM_1,SM_1", "sorting-machine-events");
    FactoryEventStreamsTopology topology = new FactoryEventStreamsTopology(
        new RawFactoryEventSplitter(objectMapper),
        new SensorDuplicateFilter(),
        new StationEventRouter(List.of(sortingTranslator)),
        stationMapper(objectMapper),
        new RawFactoryEventEnricher(objectMapper, stationMapper(objectMapper)),
        objectMapper,
        "factory.raw-events",
        "stage-orchestration",
        "factory.latest-status");
    topology.factoryEventStream(streamsBuilder);

    Properties properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "factory-event-streams-test");
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties)) {
      TestInputTopic<String, String> inputTopic = testDriver.createInputTopic(
          "factory.raw-events",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestInputTopic<String, String> commandTopic = testDriver.createInputTopic(
          "stage-orchestration",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestOutputTopic<String, String> outputTopic = testDriver.createOutputTopic(
          "sorting-machine-events",
          Serdes.String().deserializer(),
          Serdes.String().deserializer());
      TestOutputTopic<String, LatestItemStatus> latestStatusTopic = testDriver.createOutputTopic(
          "factory.latest-status",
          Serdes.String().deserializer(),
          new LatestItemStatusDeserializer());

      commandTopic.pipeInput("item-42", """
          {"commandType":"run-item-qc-command","itemIdentifier":"item-42","targetColor":"RED"}
          """, Instant.parse("2026-04-02T10:15:00Z"));
      inputTopic.pipeInput("FTFactory/SM_1", """
          {
            "id":"evt-1",
            "station":"SM_1",
            "timestamp":"2026-04-02T10:15:30Z",
            "i1_light_barrier":1,
            "i2_color_sensor":1350,
            "i3_light_barrier":1,
            "i6_light_barrier":1,
            "i7_light_barrier":1,
            "i8_light_barrier":1,
            "current_task":"detect_color",
            "current_task_duration":2.0
          }
          """);

      KeyValue<String, String> output = outputTopic.readKeyValue();
      assertEquals("color-detected", output.key);
      assertEquals("{\"eventType\":\"color-detected\",\"color\":\"red\",\"itemIdentifier\":\"item-42\"}", output.value);
      assertTrue(outputTopic.isEmpty());

      KeyValue<String, LatestItemStatus> latestStatus = latestStatusTopic.readKeyValue();
      assertEquals("item-42", latestStatus.key);
      assertEquals("item-42", latestStatus.value.itemIdentifier());
      assertEquals("SM_1", latestStatus.value.station());
      assertEquals("color-detected", latestStatus.value.outcomeType());
      assertEquals("2026-04-02T10:15:30Z", latestStatus.value.timestamp());
      assertEquals("FTFactory/SM_1", latestStatus.value.sourceTopic());
    }
  }

  @Test
  void routesRawVacuumGripperEventToExistingVacuumGripperEventTopic() {
    ObjectMapper objectMapper = new ObjectMapper();
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    VacuumGripperSensorTranslator vacuumTranslator =
        new VacuumGripperSensorTranslator(objectMapper, "FTFactory/VGR_1,VGR_1", "vacuum-gripper-events");
    FactoryEventStreamsTopology topology = new FactoryEventStreamsTopology(
        new RawFactoryEventSplitter(objectMapper),
        new SensorDuplicateFilter(),
        new StationEventRouter(List.of(vacuumTranslator)),
        stationMapper(objectMapper),
        new RawFactoryEventEnricher(objectMapper, stationMapper(objectMapper)),
        objectMapper,
        "factory.raw-events",
        "stage-orchestration",
        "factory.latest-status");
    topology.factoryEventStream(streamsBuilder);

    Properties properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "factory-event-streams-vacuum-test");
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties)) {
      TestInputTopic<String, String> inputTopic = testDriver.createInputTopic(
          "factory.raw-events",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestInputTopic<String, String> commandTopic = testDriver.createInputTopic(
          "stage-orchestration",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestOutputTopic<String, String> outputTopic = testDriver.createOutputTopic(
          "vacuum-gripper-events",
          Serdes.String().deserializer(),
          Serdes.String().deserializer());

      commandTopic.pipeInput("item-42", """
          {"commandType":"run-item-intake-command","itemIdentifier":"item-42","targetColor":"RED"}
          """, Instant.parse("2026-04-02T10:15:00Z"));
      inputTopic.pipeInput("FTFactory/VGR_1", """
          {
            "id":"evt-2",
            "station":"VGR_1",
            "timestamp":"2026-04-02T10:15:30Z",
            "i7_light_barrier":0,
            "i4_light_barrier":1,
            "current_task":"",
            "current_task_duration":0.0
          }
          """);

      KeyValue<String, String> output = outputTopic.readKeyValue();
      assertEquals("item-arrived-at-intake", output.key);
      assertEquals("{\"eventType\":\"item-arrived-at-intake\",\"itemIdentifier\":\"item-42\"}", output.value);
      assertTrue(outputTopic.isEmpty());
    }
  }

  @Test
  void latestByItemKeepsOnlyNewestStatusByEventTime() {
    ObjectMapper objectMapper = new ObjectMapper();
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    VacuumGripperSensorTranslator vacuumTranslator =
        new VacuumGripperSensorTranslator(objectMapper, "FTFactory/VGR_1,VGR_1", "vacuum-gripper-events");
    FactoryEventStreamsTopology topology = new FactoryEventStreamsTopology(
        new RawFactoryEventSplitter(objectMapper),
        new SensorDuplicateFilter(),
        new StationEventRouter(List.of(vacuumTranslator)),
        stationMapper(objectMapper),
        new RawFactoryEventEnricher(objectMapper, stationMapper(objectMapper)),
        objectMapper,
        "factory.raw-events",
        "stage-orchestration",
        "factory.latest-status");
    topology.factoryEventStream(streamsBuilder);

    Properties properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "factory-event-streams-latest-status-test");
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties)) {
      TestInputTopic<String, String> inputTopic = testDriver.createInputTopic(
          "factory.raw-events",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestInputTopic<String, String> commandTopic = testDriver.createInputTopic(
          "stage-orchestration",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestOutputTopic<String, LatestItemStatus> latestStatusTopic = testDriver.createOutputTopic(
          "factory.latest-status",
          Serdes.String().deserializer(),
          new LatestItemStatusDeserializer());

      commandTopic.pipeInput("item-42", """
          {"commandType":"run-item-intake-command","itemIdentifier":"item-42","targetColor":"RED"}
          """, Instant.parse("2026-04-02T10:15:00Z"));
      inputTopic.pipeInput("FTFactory/VGR_1", """
          {
            "id":"evt-older",
            "station":"VGR_1",
            "timestamp":"2026-04-02T10:15:30Z",
            "i7_light_barrier":0,
            "i4_light_barrier":1,
            "current_task":"",
            "current_task_duration":0.0
          }
          """);
      inputTopic.pipeInput("FTFactory/VGR_1", """
          {
            "id":"evt-newer",
            "station":"VGR_1",
            "timestamp":"2026-04-02T10:16:30Z",
            "i7_light_barrier":1,
            "i4_light_barrier":0,
            "current_task":"",
            "current_task_duration":0.0
          }
          """);
      inputTopic.pipeInput("FTFactory/VGR_1", """
          {
            "id":"evt-late-arrival",
            "station":"VGR_1",
            "timestamp":"2026-04-02T10:14:30Z",
            "i7_light_barrier":0,
            "i4_light_barrier":1,
            "current_task":"",
            "current_task_duration":0.0
          }
          """);

      List<KeyValue<String, LatestItemStatus>> latestStatuses = latestStatusTopic.readKeyValuesToList();
      KeyValue<String, LatestItemStatus> retainedLatestStatus = latestStatuses.getLast();

      assertTrue(latestStatuses.size() >= 3);
      assertEquals("item-42", retainedLatestStatus.key);
      assertEquals("item-arrived-at-output", retainedLatestStatus.value.outcomeType());
      assertEquals("2026-04-02T10:16:30Z", retainedLatestStatus.value.timestamp());
    }
  }

  private StageCommandStationMapper stationMapper(ObjectMapper objectMapper) {
    return new StageCommandStationMapper(
        objectMapper,
        "FTFactory/VGR_1,VGR_1",
        "FTFactory/OV_1,OV_1",
        "FTFactory/MM_1,MM_1",
        "FTFactory/WT_1,WT_1",
        "FTFactory/SM_1,SM_1");
  }
}
