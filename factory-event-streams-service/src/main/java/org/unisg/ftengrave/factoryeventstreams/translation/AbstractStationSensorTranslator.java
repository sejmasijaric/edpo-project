package org.unisg.ftengrave.factoryeventstreams.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unisg.ftengrave.factoryeventstreams.dto.MachineEventDto;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;

public abstract class AbstractStationSensorTranslator implements StationSensorTranslator {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractStationSensorTranslator.class);

  protected static final int LIGHT_BARRIER_INTERRUPTED = 0;

  private final Set<String> stationIdentifiers;
  private final String outputTopic;
  private final ObjectMapper objectMapper;
  private final ConcurrentMap<String, Boolean> initializedSensors = new ConcurrentHashMap<>();

  protected AbstractStationSensorTranslator(
      ObjectMapper objectMapper, String stationIdentifiers, String outputTopic) {
    this.objectMapper = objectMapper;
    this.stationIdentifiers = Arrays.stream(stationIdentifiers.split(","))
        .map(String::trim)
        .filter(identifier -> !identifier.isBlank())
        .collect(Collectors.toUnmodifiableSet());
    this.outputTopic = outputTopic;
  }

  @Override
  public boolean supports(SensorLevelEvent event) {
    return event != null
        && (stationIdentifiers.contains(event.getStation())
        || stationIdentifiers.contains(event.getSourceTopic()));
  }

  protected Optional<TranslatedMachineEvent> mapLightBarrierEvent(
      SensorLevelEvent event, String sensorName, String interruptedEvent, String releasedEvent) {
    if (!sensorName.equals(event.getSensorName())) {
      return Optional.empty();
    }

    Optional<Integer> currentState = intValue(event);
    if (currentState.isEmpty()) {
      return Optional.empty();
    }

    String sensorKey = event.sensorKey();
    boolean alreadyInitialized = initializedSensors.put(sensorKey, true) != null;
    if (currentState.get() == LIGHT_BARRIER_INTERRUPTED) {
      return Optional.of(toTranslatedMachineEvent(interruptedEvent, event));
    }
    if (alreadyInitialized) {
      return Optional.of(toTranslatedMachineEvent(releasedEvent, event));
    }
    return Optional.empty();
  }

  protected TranslatedMachineEvent toTranslatedMachineEvent(String eventType, SensorLevelEvent event) {
    try {
      LOGGER.info("Translated factory sensor event to {} on {}", eventType, outputTopic);
      return new TranslatedMachineEvent(
          outputTopic,
          eventType,
          objectMapper.writeValueAsString(new MachineEventDto(eventType, event.getItemIdentifier())),
          event.getItemIdentifier(),
          event.getStation(),
          event.getTimestamp(),
          event.getSourceTopic());
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize machine event", exception);
    }
  }

  protected Optional<Integer> intValue(SensorLevelEvent event) {
    try {
      return Optional.of(Integer.parseInt(event.getSensorValue()));
    } catch (NumberFormatException exception) {
      LOGGER.warn("Ignoring non-integer sensor value for {}", event.sensorKey());
      return Optional.empty();
    }
  }
}
