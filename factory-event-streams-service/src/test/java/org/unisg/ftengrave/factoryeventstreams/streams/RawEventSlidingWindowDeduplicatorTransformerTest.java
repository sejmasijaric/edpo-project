package org.unisg.ftengrave.factoryeventstreams.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RawEventSlidingWindowDeduplicatorTransformerTest {

  @TempDir
  Path stateDir;

  @Test
  void filtersDuplicateRawEventsWithinOneSecondWindow() {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    streamsBuilder.addStateStore(Stores.windowStoreBuilder(
        Stores.persistentWindowStore(
            RawEventSlidingWindowDeduplicatorTransformer.STORE_NAME,
            RawEventSlidingWindowDeduplicatorTransformer.WINDOW,
            RawEventSlidingWindowDeduplicatorTransformer.WINDOW,
            false),
        Serdes.String(),
        Serdes.Long()));
    streamsBuilder.stream("raw-input", Consumed.with(Serdes.String(), Serdes.String()))
        .transform(
            RawEventSlidingWindowDeduplicatorTransformer::new,
            RawEventSlidingWindowDeduplicatorTransformer.STORE_NAME)
        .to("raw-output", Produced.with(Serdes.String(), Serdes.String()));

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties())) {
      TestInputTopic<String, String> inputTopic = testDriver.createInputTopic(
          "raw-input",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestOutputTopic<String, String> outputTopic = testDriver.createOutputTopic(
          "raw-output",
          Serdes.String().deserializer(),
          Serdes.String().deserializer());

      inputTopic.pipeInput("FTFactory/VGR_1", rawPayload(), Instant.ofEpochMilli(1_000L));
      inputTopic.pipeInput("FTFactory/VGR_1", rawPayload(), Instant.ofEpochMilli(1_999L));

      assertEquals(rawPayload(), outputTopic.readValue());
      assertTrue(outputTopic.isEmpty());
    }
  }

  @Test
  void forwardsSameRawEventAfterOneSecondWindowHasPassed() {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    streamsBuilder.addStateStore(Stores.windowStoreBuilder(
        Stores.persistentWindowStore(
            RawEventSlidingWindowDeduplicatorTransformer.STORE_NAME,
            RawEventSlidingWindowDeduplicatorTransformer.WINDOW,
            RawEventSlidingWindowDeduplicatorTransformer.WINDOW,
            false),
        Serdes.String(),
        Serdes.Long()));
    streamsBuilder.stream("raw-input", Consumed.with(Serdes.String(), Serdes.String()))
        .transform(
            RawEventSlidingWindowDeduplicatorTransformer::new,
            RawEventSlidingWindowDeduplicatorTransformer.STORE_NAME)
        .to("raw-output", Produced.with(Serdes.String(), Serdes.String()));

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties())) {
      TestInputTopic<String, String> inputTopic = testDriver.createInputTopic(
          "raw-input",
          Serdes.String().serializer(),
          Serdes.String().serializer());
      TestOutputTopic<String, String> outputTopic = testDriver.createOutputTopic(
          "raw-output",
          Serdes.String().deserializer(),
          Serdes.String().deserializer());

      inputTopic.pipeInput("FTFactory/VGR_1", rawPayload(), Instant.ofEpochMilli(1_000L));
      inputTopic.pipeInput("FTFactory/VGR_1", rawPayload(), Instant.ofEpochMilli(2_001L));

      assertEquals(rawPayload(), outputTopic.readValue());
      assertEquals(rawPayload(), outputTopic.readValue());
      assertTrue(outputTopic.isEmpty());
    }
  }

  private Properties properties() {
    Properties properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "raw-event-dedup-test");
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
    properties.put(StreamsConfig.STATE_DIR_CONFIG, stateDir.toString());
    return properties;
  }

  private String rawPayload() {
    return """
        {
          "id":"evt-1",
          "station":"VGR_1",
          "timestamp":"2026-04-02T10:15:30Z",
          "i7_light_barrier":0
        }
        """;
  }
}
