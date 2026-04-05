package org.unisg.ftengrave.vacuumgripperintegrationservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.vacuumgripperintegrationservice.dto.VacuumGripperEventDto;
import org.unisg.ftengrave.vacuumgripperintegrationservice.dto.VacuumGripperEventTransformationDto;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;

@Service
public class VacuumGripperEventFilter implements MqttEventFilter<VacuumGripperEventDto> {

  private static final int LIGHT_BARRIER_INTERRUPTED = 0;
  private static final String ITEM_ARRIVED_AT_INPUT = "item-arrived-at-intake";
  private static final String ITEM_LEFT_INPUT = "item-left-intake";
  private static final String ITEM_ARRIVED_AT_OUTPUT = "item-arrived-at-output";
  private static final String ITEM_LEFT_OUTPUT = "item-left-output";

  private final ObjectMapper objectMapper;
  private VacuumGripperEventTransformationDto lastSnapshot;

  public VacuumGripperEventFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public synchronized Optional<VacuumGripperEventDto> filter(String topic, String rawPayload) {
    try {
      VacuumGripperEventTransformationDto dto = parse(rawPayload);
      Optional<VacuumGripperEventDto> event = mapEventType(dto);
      lastSnapshot = dto;
      return event;
    } catch (Exception exception) {
      return Optional.empty();
    }
  }

  private VacuumGripperEventTransformationDto parse(String rawPayload) throws Exception {
    JsonNode root = objectMapper.readTree(rawPayload);

    if (!root.hasNonNull("timestamp")
        || !root.hasNonNull("i7_light_barrier")
        || !root.hasNonNull("i4_light_barrier")
        || !root.hasNonNull("current_task")
        || !root.hasNonNull("current_task_duration")) {
      throw new IllegalArgumentException("Incomplete MQTT payload for vacuum-gripper event mapping");
    }

    VacuumGripperEventTransformationDto dto = new VacuumGripperEventTransformationDto();
    dto.setTimestamp(root.get("timestamp").asText());
    dto.setI7LightBarrier(root.get("i7_light_barrier").asInt());
    dto.setI4LightBarrier(root.get("i4_light_barrier").asInt());
    dto.setCurrentTask(root.get("current_task").asText());
    dto.setCurrentTaskDuration(root.get("current_task_duration").asDouble());
    return dto;
  }

  private Optional<VacuumGripperEventDto> mapEventType(VacuumGripperEventTransformationDto dto) {
    Optional<String> inputEvent = mapLightBarrierEvent(
        lastSnapshot == null ? null : lastSnapshot.getI7LightBarrier(),
        dto.getI7LightBarrier(),
        ITEM_ARRIVED_AT_INPUT,
        ITEM_LEFT_INPUT);
    if (inputEvent.isPresent()) {
      return inputEvent.map(VacuumGripperEventDto::new);
    }

    Optional<String> outputEvent = mapLightBarrierEvent(
        lastSnapshot == null ? null : lastSnapshot.getI4LightBarrier(),
        dto.getI4LightBarrier(),
        ITEM_ARRIVED_AT_OUTPUT,
        ITEM_LEFT_OUTPUT);
    return outputEvent.map(VacuumGripperEventDto::new);
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
