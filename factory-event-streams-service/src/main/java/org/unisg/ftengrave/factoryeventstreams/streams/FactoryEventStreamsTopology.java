package org.unisg.ftengrave.factoryeventstreams.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.serialization.TranslatedMachineEventDeserializer;
import org.unisg.ftengrave.factoryeventstreams.serialization.TranslatedMachineEventSerializer;

@Component
public class FactoryEventStreamsTopology {

  private static final Logger LOGGER = LoggerFactory.getLogger(FactoryEventStreamsTopology.class);

  private final RawFactoryEventSplitter splitter;
  private final SensorDuplicateFilter duplicateFilter;
  private final StationEventRouter stationEventRouter;
  private final ObjectMapper objectMapper;
  private final String rawFactoryEventTopic;

  public FactoryEventStreamsTopology(
      RawFactoryEventSplitter splitter,
      SensorDuplicateFilter duplicateFilter,
      StationEventRouter stationEventRouter,
      ObjectMapper objectMapper,
      @Value("${kafka.topic.raw-factory-event}") String rawFactoryEventTopic) {
    this.splitter = splitter;
    this.duplicateFilter = duplicateFilter;
    this.stationEventRouter = stationEventRouter;
    this.objectMapper = objectMapper;
    this.rawFactoryEventTopic = rawFactoryEventTopic;
  }

  @Bean
  public KStream<String, TranslatedMachineEvent> factoryEventStream(StreamsBuilder streamsBuilder) {
    streamsBuilder.addStateStore(Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(SensorDuplicateFilterTransformer.STORE_NAME),
        Serdes.String(),
        Serdes.String()));

    KStream<String, TranslatedMachineEvent> translatedEvents = streamsBuilder
        .stream(rawFactoryEventTopic, Consumed.with(Serdes.String(), Serdes.String())
            .withTimestampExtractor(new RawFactoryEventTimestampExtractor(objectMapper)))
        .peek((sourceTopic, rawPayload) ->
            LOGGER.debug("Received raw factory event from MQTT topic {}", sourceTopic))
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

    return translatedEvents;
  }
}
