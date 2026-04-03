package org.unisg.ftengrave.engraverintegrationservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.engraverintegrationservice.dto.EngraverEventDto;
import org.unisg.ftengrave.engraverintegrationservice.dto.EngraverEventTransformationDto;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;

@Service
public class EngraverEventFilter implements MqttEventFilter<EngraverEventDto> {

  private static final int LIGHT_BARRIER_INTERRUPTED = 0;
  private static final String ITEM_ARRIVED_AT_ENGRAVER_SINK = "item-arrived-at-engraver-sink";
  private static final String ITEM_LEFT_ENGRAVER_SINK = "item-left-engraver-sink";

  private final ObjectMapper objectMapper;
  private EngraverEventTransformationDto lastSnapshot;

  public EngraverEventFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public synchronized Optional<EngraverEventDto> filter(String topic, String rawPayload) {
    try {
      EngraverEventTransformationDto dto = parse(rawPayload);
      Optional<EngraverEventDto> event = mapEventType(dto);
      lastSnapshot = dto;
      return event;
    } catch (Exception exception) {
      return Optional.empty();
    }
  }

  private EngraverEventTransformationDto parse(String rawPayload) throws Exception {
    JsonNode root = objectMapper.readTree(rawPayload);

    if (!root.hasNonNull("timestamp")
        || !root.hasNonNull("i5_light_barrier")
        || !root.hasNonNull("current_task")
        || !root.hasNonNull("current_task_duration")) {
      throw new IllegalArgumentException("Incomplete MQTT payload for engraver event mapping");
    }

    EngraverEventTransformationDto dto = new EngraverEventTransformationDto();
    dto.setTimestamp(root.get("timestamp").asText());
    dto.setI5LightBarrier(root.get("i5_light_barrier").asInt());
    dto.setCurrentTask(root.get("current_task").asText());
    dto.setCurrentTaskDuration(root.get("current_task_duration").asDouble());
    return dto;
  }

  private Optional<EngraverEventDto> mapEventType(EngraverEventTransformationDto dto) {
    Integer previousState = lastSnapshot == null ? null : lastSnapshot.getI5LightBarrier();
    return mapLightBarrierEvent(
        previousState,
        dto.getI5LightBarrier(),
        ITEM_ARRIVED_AT_ENGRAVER_SINK,
        ITEM_LEFT_ENGRAVER_SINK).map(EngraverEventDto::new);
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
