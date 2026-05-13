package org.unisg.ftengrave.factoryeventstreams.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.LatestItemStatus;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.serialization.LatestItemStatusDeserializer;
import org.unisg.ftengrave.factoryeventstreams.serialization.LatestItemStatusSerializer;
import org.unisg.ftengrave.factoryeventstreams.serialization.TranslatedMachineEventDeserializer;
import org.unisg.ftengrave.factoryeventstreams.serialization.TranslatedMachineEventSerializer;

@Component
public class FactoryEventStreamsTopology {

  private static final Logger LOGGER = LoggerFactory.getLogger(FactoryEventStreamsTopology.class);
  private static final String ITEM_STATION_STORE_NAME = "factory-item-station-store";
  public static final String LATEST_BY_ITEM_STORE_NAME = "factory-latest-by-item-v1-store";
  private static final DateTimeFormatter FACTORY_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

  private final RawFactoryEventSplitter splitter;
  private final SensorDuplicateFilter duplicateFilter;
  private final StationEventRouter stationEventRouter;
  private final StageCommandStationMapper stationMapper;
  private final RawFactoryEventEnricher eventEnricher;
  private final ObjectMapper objectMapper;
  private final String rawFactoryEventTopic;
  private final String stageOrchestrationTopic;
  private final String latestStatusTopic;

  public FactoryEventStreamsTopology(
      RawFactoryEventSplitter splitter,
      SensorDuplicateFilter duplicateFilter,
      StationEventRouter stationEventRouter,
      StageCommandStationMapper stationMapper,
      RawFactoryEventEnricher eventEnricher,
      ObjectMapper objectMapper,
      @Value("${kafka.topic.raw-factory-event}") String rawFactoryEventTopic,
      @Value("${kafka.topic.stage-orchestration}") String stageOrchestrationTopic,
      @Value("${kafka.topic.latest-item-status}") String latestStatusTopic) {
    this.splitter = splitter;
    this.duplicateFilter = duplicateFilter;
    this.stationEventRouter = stationEventRouter;
    this.stationMapper = stationMapper;
    this.eventEnricher = eventEnricher;
    this.objectMapper = objectMapper;
    this.rawFactoryEventTopic = rawFactoryEventTopic;
    this.stageOrchestrationTopic = stageOrchestrationTopic;
    this.latestStatusTopic = latestStatusTopic;
  }

  @Bean
  public KStream<String, TranslatedMachineEvent> factoryEventStream(StreamsBuilder streamsBuilder) {
    streamsBuilder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(SensorDuplicateFilterTransformer.STORE_NAME),
        Serdes.String(),
        Serdes.String()));
    streamsBuilder.addStateStore(Stores.windowStoreBuilder(
        Stores.persistentWindowStore(
            RawEventSlidingWindowDeduplicatorTransformer.STORE_NAME,
            RawEventSlidingWindowDeduplicatorTransformer.WINDOW,
            RawEventSlidingWindowDeduplicatorTransformer.WINDOW,
            false),
        Serdes.String(),
        Serdes.Long()));

    KTable<String, String> itemStationTable = streamsBuilder
        .stream(stageOrchestrationTopic, Consumed.with(Serdes.String(), Serdes.String()))
        .transform(() -> new StageCommandStationTransformer(stationMapper))
        .toTable(Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(ITEM_STATION_STORE_NAME)
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.String()));

    KStream<String, String> rawEvents = streamsBuilder
        .stream(rawFactoryEventTopic, Consumed.with(Serdes.String(), Serdes.String())
            .withTimestampExtractor(new RawFactoryEventTimestampExtractor(objectMapper)))
        .peek((sourceTopic, rawPayload) ->
            LOGGER.debug("Received raw factory event from MQTT topic {}", sourceTopic));

    KStream<String, TranslatedMachineEvent> translatedEvents = rawEvents
        .transform(
            RawEventSlidingWindowDeduplicatorTransformer::new,
            RawEventSlidingWindowDeduplicatorTransformer.STORE_NAME)
        .mapValues(eventEnricher::attachSourceTopic)
        .selectKey(eventEnricher::stationKey)
        .leftJoin(
            itemStationTable,
            eventEnricher::enrich,
            Joined.with(Serdes.String(), Serdes.String(), Serdes.String()))
        .flatMap(splitter::split)
        .transform(
            () -> new SensorDuplicateFilterTransformer(duplicateFilter),
            SensorDuplicateFilterTransformer.STORE_NAME)
        .flatMapValues(stationEventRouter::route)
        .selectKey((key, translatedEvent) -> translatedEvent.key());

    translatedEvents.to(
        (key, translatedEvent, context) -> translatedEvent.topic(),
        Produced.with(
            Serdes.String(),
            Serdes.serdeFrom(
                new TranslatedMachineEventSerializer(),
                new TranslatedMachineEventDeserializer())));

    Serde<LatestItemStatus> latestStatusSerde = Serdes.serdeFrom(
        new LatestItemStatusSerializer(),
        new LatestItemStatusDeserializer());

    translatedEvents
        .filter((key, translatedEvent) -> hasKnownItemIdentifier(translatedEvent))
        .selectKey((key, translatedEvent) -> translatedEvent.itemIdentifier())
        .mapValues(this::toLatestItemStatus)
        .groupByKey(Grouped.with(Serdes.String(), latestStatusSerde))
        .reduce(
            (current, next) -> next.isAfterOrSameAs(current) ? next : current,
            Materialized.<String, LatestItemStatus, KeyValueStore<Bytes, byte[]>>as(LATEST_BY_ITEM_STORE_NAME)
                .withKeySerde(Serdes.String())
                .withValueSerde(latestStatusSerde))
        .toStream()
        .to(latestStatusTopic, Produced.with(Serdes.String(), latestStatusSerde));

    return translatedEvents;
  }

  private boolean hasKnownItemIdentifier(TranslatedMachineEvent translatedEvent) {
    return translatedEvent != null
        && translatedEvent.itemIdentifier() != null
        && !translatedEvent.itemIdentifier().isBlank()
        && !RawFactoryEventEnricher.UNKNOWN_ITEM_IDENTIFIER.equals(translatedEvent.itemIdentifier());
  }

  private LatestItemStatus toLatestItemStatus(TranslatedMachineEvent event) {
    long eventTimestamp = eventTimestampEpochMillis(event.timestamp());
    return new LatestItemStatus(
        event.itemIdentifier(),
        event.station(),
        event.key(),
        Instant.ofEpochMilli(eventTimestamp).toString(),
        event.sourceTopic(),
        eventTimestamp);
  }

  private long eventTimestampEpochMillis(String timestamp) {
    if (timestamp != null && !timestamp.isBlank()) {
      try {
        return Instant.parse(timestamp).toEpochMilli();
      } catch (Exception ignored) {
        try {
          return LocalDateTime.parse(timestamp, FACTORY_TIMESTAMP_FORMATTER)
              .atZone(ZoneId.systemDefault())
              .toInstant()
              .toEpochMilli();
        } catch (Exception exception) {
          LOGGER.warn("Could not parse translated event timestamp {}", timestamp, exception);
        }
      }
    }
    return System.currentTimeMillis();
  }
}
