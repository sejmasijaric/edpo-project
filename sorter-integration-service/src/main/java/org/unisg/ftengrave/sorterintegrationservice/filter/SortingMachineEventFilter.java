package org.unisg.ftengrave.sorterintegrationservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineEventDto;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineEventTransformationDto;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;

@Service
public class SortingMachineEventFilter implements MqttEventFilter<SortingMachineEventDto> {

  private static final int LIGHT_BARRIER_INTERRUPTED = 0;
  private static final int COLOR_SENSOR_DETECTION_THRESHOLD = 1700;
  private static final int WHITE_UPPER_BOUNDARY = 1200;
  private static final int RED_UPPER_BOUNDARY = 1500;
  private static final String ITEM_ARRIVED_AT_COLOR_SENSOR = "item-arrived-at-color-sensor";
  private static final String ITEM_LEFT_COLOR_SENSOR = "item-left-color-sensor";
  private static final String COLOR_DETECTED_EVENT = "color-detected";
  private static final String ITEM_ARRIVED_AT_REJECTION_SINK = "item-arrived-at-rejection-sink";
  private static final String ITEM_LEFT_REJECTION_SINK = "item-left-rejection-sink";
  private static final String ITEM_ARRIVED_AT_SHIPPING_SINK = "item-arrived-at-shipping-sink";
  private static final String ITEM_LEFT_SHIPPING_SINK = "item-left-shipping-sink";
  private static final String ITEM_ARRIVED_AT_RETRY_SINK = "item-arrived-at-retry-sink";
  private static final String ITEM_LEFT_RETRY_SINK = "item-left-retry-sink";

  private final ObjectMapper objectMapper;
  private SortingMachineEventTransformationDto lastSnapshot;

  public SortingMachineEventFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public synchronized Optional<SortingMachineEventDto> filter(String topic, String rawPayload) {
    try {
      SortingMachineEventTransformationDto dto = parse(rawPayload);
      Optional<SortingMachineEventDto> event = mapEventType(dto);
      lastSnapshot = dto;

      if (event.isEmpty()) {
        return Optional.empty();
      }
      return event;
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private SortingMachineEventTransformationDto parse(String rawPayload) throws Exception {
    JsonNode root = objectMapper.readTree(rawPayload);

    if (!root.hasNonNull("timestamp")
        || !root.hasNonNull("i1_light_barrier")
        || !root.hasNonNull("i2_color_sensor")
        || !root.hasNonNull("i3_light_barrier")
        || !root.hasNonNull("i6_light_barrier")
        || !root.hasNonNull("i7_light_barrier")
        || !root.hasNonNull("i8_light_barrier")
        || !root.hasNonNull("current_task")
        || !root.hasNonNull("current_task_duration")) {
      throw new IllegalArgumentException("Incomplete MQTT payload for sorting-machine event mapping");
    }

    SortingMachineEventTransformationDto dto = new SortingMachineEventTransformationDto();
    dto.setTimestamp(root.get("timestamp").asText());
    dto.setI1LightBarrier(root.get("i1_light_barrier").asInt());
    dto.setI2ColorSensor(root.get("i2_color_sensor").asInt());
    dto.setI3LightBarrier(root.get("i3_light_barrier").asInt());
    dto.setI6LightBarrier(root.get("i6_light_barrier").asInt());
    dto.setI7LightBarrier(root.get("i7_light_barrier").asInt());
    dto.setI8LightBarrier(root.get("i8_light_barrier").asInt());
    dto.setCurrentTask(root.get("current_task").asText());
    dto.setCurrentTaskDuration(root.get("current_task_duration").asDouble());
    return dto;
  }

  private String determineColor(SortingMachineEventTransformationDto dto) {
    int colorValue = dto.getI2ColorSensor();
    if (colorValue <= WHITE_UPPER_BOUNDARY) {
      return "white";
    } else if (colorValue < RED_UPPER_BOUNDARY) {
      return "red";
    } else {
      return "blue";
    }
  }

  private Optional<SortingMachineEventDto> mapEventType(SortingMachineEventTransformationDto dto) {
    Optional<SortingMachineEventDto> colorSensorEvent = mapColorSensorEvent(dto);
    if (colorSensorEvent.isPresent()) {
      return colorSensorEvent;
    }

    Optional<String> qcEvent = mapLightBarrierEvent(
        lastSnapshot == null ? null : lastSnapshot.getI1LightBarrier(),
        dto.getI1LightBarrier(),
        ITEM_ARRIVED_AT_COLOR_SENSOR,
        ITEM_LEFT_COLOR_SENSOR);
    if (qcEvent.isPresent()) {
      return qcEvent.map(SortingMachineEventDto::new);
    }

    Optional<String> rejectionSinkEvent = mapLightBarrierEvent(
        lastSnapshot == null ? null : lastSnapshot.getI6LightBarrier(),
        dto.getI6LightBarrier(),
        ITEM_ARRIVED_AT_REJECTION_SINK,
        ITEM_LEFT_REJECTION_SINK);
    if (rejectionSinkEvent.isPresent()) {
      return rejectionSinkEvent.map(SortingMachineEventDto::new);
    }

    Optional<String> shippingSinkEvent = mapLightBarrierEvent(
        lastSnapshot == null ? null : lastSnapshot.getI7LightBarrier(),
        dto.getI7LightBarrier(),
        ITEM_ARRIVED_AT_SHIPPING_SINK,
        ITEM_LEFT_SHIPPING_SINK);
    if (shippingSinkEvent.isPresent()) {
      return shippingSinkEvent.map(SortingMachineEventDto::new);
    }

    Optional<String> retrySinkEvent = mapLightBarrierEvent(
        lastSnapshot == null ? null : lastSnapshot.getI8LightBarrier(),
        dto.getI8LightBarrier(),
        ITEM_ARRIVED_AT_RETRY_SINK,
        ITEM_LEFT_RETRY_SINK);
    if (retrySinkEvent.isPresent()) {
      return retrySinkEvent.map(SortingMachineEventDto::new);
    }

    return Optional.empty();
  }

  private Optional<SortingMachineEventDto> mapColorSensorEvent(SortingMachineEventTransformationDto dto) {
    Integer previousColorValue = lastSnapshot == null ? null : lastSnapshot.getI2ColorSensor();
    int currentColorValue = dto.getI2ColorSensor();

    boolean isDetectedNow = currentColorValue < COLOR_SENSOR_DETECTION_THRESHOLD;
    boolean wasDetectedBefore =
        previousColorValue != null && previousColorValue < COLOR_SENSOR_DETECTION_THRESHOLD;

    if (!isDetectedNow || wasDetectedBefore) {
      return Optional.empty();
    }

    return Optional.of(new SortingMachineEventDto(COLOR_DETECTED_EVENT, determineColor(dto)));
  }

  private Optional<String> mapLightBarrierEvent(
      Integer previousState, int currentState, String interruptedEvent, String releasedEvent) {
    if (previousState == null) {
      return currentState == LIGHT_BARRIER_INTERRUPTED ? Optional.of(interruptedEvent) : Optional.empty();
    }
    if (previousState == currentState) {
      return Optional.empty();
    }
    if (currentState == LIGHT_BARRIER_INTERRUPTED) {
      return Optional.of(interruptedEvent);
    }
    return Optional.of(releasedEvent);
  }
}
