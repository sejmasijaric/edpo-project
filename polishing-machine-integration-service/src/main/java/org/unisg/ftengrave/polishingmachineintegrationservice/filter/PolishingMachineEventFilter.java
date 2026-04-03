package org.unisg.ftengrave.polishingmachineintegrationservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.polishingmachineintegrationservice.dto.PolishingMachineEventDto;
import org.unisg.ftengrave.polishingmachineintegrationservice.dto.PolishingMachineEventTransformationDto;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;

@Service
public class PolishingMachineEventFilter implements MqttEventFilter<PolishingMachineEventDto> {

  private static final int LIGHT_BARRIER_INTERRUPTED = 0;
  private static final String ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT =
      "item-arrived-at-polishing-machine-output";
  private static final String ITEM_LEFT_POLISHING_MACHINE_OUTPUT =
      "item-left-polishing-machine-output";

  private final ObjectMapper objectMapper;
  private PolishingMachineEventTransformationDto lastSnapshot;

  public PolishingMachineEventFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public synchronized Optional<PolishingMachineEventDto> filter(String topic, String rawPayload) {
    try {
      PolishingMachineEventTransformationDto dto = parse(rawPayload);
      Optional<PolishingMachineEventDto> event = mapEventType(dto);
      lastSnapshot = dto;
      return event;
    } catch (Exception exception) {
      return Optional.empty();
    }
  }

  private PolishingMachineEventTransformationDto parse(String rawPayload) throws Exception {
    JsonNode root = objectMapper.readTree(rawPayload);

    if (!root.hasNonNull("timestamp")
        || !root.hasNonNull("i4_light_barrier")
        || !root.hasNonNull("current_task")
        || !root.hasNonNull("current_task_duration")) {
      throw new IllegalArgumentException("Incomplete MQTT payload for polishing-machine event mapping");
    }

    PolishingMachineEventTransformationDto dto = new PolishingMachineEventTransformationDto();
    dto.setTimestamp(root.get("timestamp").asText());
    dto.setI4LightBarrier(root.get("i4_light_barrier").asInt());
    dto.setCurrentTask(root.get("current_task").asText());
    dto.setCurrentTaskDuration(root.get("current_task_duration").asDouble());
    return dto;
  }

  private Optional<PolishingMachineEventDto> mapEventType(PolishingMachineEventTransformationDto dto) {
    Integer previousState = lastSnapshot == null ? null : lastSnapshot.getI4LightBarrier();
    return mapLightBarrierEvent(
        previousState,
        dto.getI4LightBarrier(),
        ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT,
        ITEM_LEFT_POLISHING_MACHINE_OUTPUT).map(PolishingMachineEventDto::new);
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
