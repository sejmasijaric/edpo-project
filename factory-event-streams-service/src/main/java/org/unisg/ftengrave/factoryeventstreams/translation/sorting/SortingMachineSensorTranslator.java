package org.unisg.ftengrave.factoryeventstreams.translation.sorting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.SortingMachineEventDto;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.translation.StationSensorTranslator;

@Component
public class SortingMachineSensorTranslator implements StationSensorTranslator {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SortingMachineSensorTranslator.class);

  private static final int LIGHT_BARRIER_INTERRUPTED = 0;
  private static final int COLOR_SENSOR_DETECTION_THRESHOLD = 1700;
  private static final int WHITE_UPPER_BOUNDARY = 1200;
  private static final int RED_UPPER_BOUNDARY = 1500;
  private static final String COLOR_DETECTED_EVENT = "color-detected";

  private final Set<String> stationIdentifiers;
  private final String outputTopic;
  private final ObjectMapper objectMapper;
  private final Map<String, Boolean> initializedSensors = new ConcurrentHashMap<>();
  private final Map<String, Boolean> colorDetectedByStation = new ConcurrentHashMap<>();

  public SortingMachineSensorTranslator(
      ObjectMapper objectMapper,
      @Value("${factory-streams.qc.station-identifiers:FTFactory/SM_1,SM_1,sm_1,sorting-machine,sorter}")
      String stationIdentifiers,
      @Value("${kafka.topic.sorting-machine-event}") String outputTopic) {
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

  @Override
  public List<TranslatedMachineEvent> translate(SensorLevelEvent event) {
    Optional<SortingMachineEventDto> translatedEvent = switch (event.getSensorName()) {
      case "i1_light_barrier" -> mapLightBarrierEvent(
          event, "item-arrived-at-color-sensor", "item-left-color-sensor");
      case "i2_color_sensor" -> mapColorSensorEvent(event);
      case "i3_light_barrier" -> mapLightBarrierEvent(
          event, "item-arrived-at-qc", "item-left-qc");
      case "i6_light_barrier" -> mapLightBarrierEvent(
          event, "item-arrived-at-rejection-sink", "item-left-rejection-sink");
      case "i7_light_barrier" -> mapLightBarrierEvent(
          event, "item-arrived-at-shipping-sink", "item-left-shipping-sink");
      case "i8_light_barrier" -> mapLightBarrierEvent(
          event, "item-arrived-at-retry-sink", "item-left-retry-sink");
      default -> Optional.empty();
    };

    return translatedEvent
        .map(translated -> toTranslatedMachineEvent(translated, event))
        .map(List::of)
        .orElseGet(List::of);
  }

  private TranslatedMachineEvent toTranslatedMachineEvent(
      SortingMachineEventDto event, SensorLevelEvent sourceEvent) {
    try {
      LOGGER.info("Translated sorting-machine sensor event to business event {}",
          event.getEventType());
      return new TranslatedMachineEvent(
          outputTopic,
          event.getEventType(),
          objectMapper.writeValueAsString(event),
          sourceEvent.getItemIdentifier(),
          sourceEvent.getStation(),
          sourceEvent.getTimestamp(),
          sourceEvent.getSourceTopic());
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize sorting-machine event", exception);
    }
  }

  private Optional<SortingMachineEventDto> mapColorSensorEvent(SensorLevelEvent event) {
    Optional<Integer> currentValue = intValue(event);
    if (currentValue.isEmpty()) {
      return Optional.empty();
    }

    boolean detectedNow = currentValue.get() < COLOR_SENSOR_DETECTION_THRESHOLD;
    boolean detectedBefore = colorDetectedByStation.getOrDefault(event.getStation(), false);
    colorDetectedByStation.put(event.getStation(), detectedNow);

    if (!detectedNow || detectedBefore) {
      return Optional.empty();
    }

    return Optional.of(new SortingMachineEventDto(
        COLOR_DETECTED_EVENT, determineColor(currentValue.get()), event.getItemIdentifier()));
  }

  private Optional<SortingMachineEventDto> mapLightBarrierEvent(
      SensorLevelEvent event, String interruptedEvent, String releasedEvent) {
    Optional<Integer> currentState = intValue(event);
    if (currentState.isEmpty()) {
      return Optional.empty();
    }

    String sensorKey = event.sensorKey();
    boolean alreadyInitialized = initializedSensors.put(sensorKey, true) != null;
    if (currentState.get() == LIGHT_BARRIER_INTERRUPTED) {
      return Optional.of(new SortingMachineEventDto(interruptedEvent, null, event.getItemIdentifier()));
    }
    if (alreadyInitialized) {
      return Optional.of(new SortingMachineEventDto(releasedEvent, null, event.getItemIdentifier()));
    }
    return Optional.empty();
  }

  private String determineColor(int colorValue) {
    if (colorValue <= WHITE_UPPER_BOUNDARY) {
      return "white";
    }
    if (colorValue < RED_UPPER_BOUNDARY) {
      return "red";
    }
    return "blue";
  }

  private Optional<Integer> intValue(SensorLevelEvent event) {
    try {
      return Optional.of(Integer.parseInt(event.getSensorValue()));
    } catch (NumberFormatException exception) {
      LOGGER.warn("Ignoring non-integer sorting-machine sensor value for {}", event.sensorKey());
      return Optional.empty();
    }
  }
}
