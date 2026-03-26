package org.unisg.ftengrave.sorterintegrationsetrvice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.sorterintegrationsetrvice.dto.SortingMachineEventDto;
import org.unisg.ftengrave.sorterintegrationsetrvice.dto.SortingMachineEventTransformationDto;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;

@Service
public class SortingMachineEventFilter implements MqttEventFilter<SortingMachineEventDto> {

  private final ObjectMapper objectMapper;
  private String lastPublishedEventType;

  public SortingMachineEventFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public synchronized Optional<SortingMachineEventDto> filter(String topic, String rawPayload) {
    try {
      SortingMachineEventTransformationDto dto = parse(rawPayload);
      Optional<String> eventType = mapDetectedColorEvent(dto).or(() -> mapEventType(dto));

      if (eventType.isEmpty()) {
        return Optional.empty();
      }

      if (eventType.get().equals(lastPublishedEventType)) {
        return Optional.empty();
      }

      lastPublishedEventType = eventType.get();
      return eventType.map(SortingMachineEventDto::new);
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

  private Optional<String> mapDetectedColorEvent(SortingMachineEventTransformationDto dto) {
    if (!(dto.getI2ColorSensor() <= 1700)) {
      return Optional.empty();
    }

    return Optional.of("detected-color-" + determineColor(dto));
  }

  private String determineColor(SortingMachineEventTransformationDto dto) {
    int colorValue = dto.getI2ColorSensor();
    int redLowerBoundary = 1200;
    int blueLowerBoundary = 1500;
    if (colorValue <= redLowerBoundary) {
      return "white";
    } else if (redLowerBoundary < colorValue && colorValue < blueLowerBoundary) {
      return "red";
    } else {
      return "blue";
    }
  }

  private Optional<String> mapEventType(SortingMachineEventTransformationDto dto) {
    if (dto.getI6LightBarrier() == 0) {
      return Optional.of("sort-to-reject");
    }
    if (dto.getI7LightBarrier() == 0) {
      return Optional.of("sort-to-shipping");
    }
    if (dto.getI8LightBarrier() == 0) {
      return Optional.of("sort-to-retry");
    }
    return Optional.empty();
  }
}
