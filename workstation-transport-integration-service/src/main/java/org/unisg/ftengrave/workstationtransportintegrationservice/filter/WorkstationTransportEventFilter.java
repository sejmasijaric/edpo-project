package org.unisg.ftengrave.workstationtransportintegrationservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.workstationtransportintegrationservice.dto.WorkstationTransportEventDto;
import org.unisg.ftengrave.workstationtransportintegrationservice.dto.WorkstationTransportEventTransformationDto;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;

@Service
public class WorkstationTransportEventFilter
    implements MqttEventFilter<WorkstationTransportEventDto> {

  private static final String READY_STATE = "ready";
  private static final String MOVE_COMPLETED_EVENT = "wt-move-completed";

  private final ObjectMapper objectMapper;
  private WorkstationTransportEventTransformationDto lastSnapshot;

  public WorkstationTransportEventFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public synchronized Optional<WorkstationTransportEventDto> filter(String topic, String rawPayload) {
    try {
      WorkstationTransportEventTransformationDto dto = parse(rawPayload);
      Optional<WorkstationTransportEventDto> event = mapEventType(dto);
      lastSnapshot = dto;
      return event;
    } catch (Exception exception) {
      return Optional.empty();
    }
  }

  private WorkstationTransportEventTransformationDto parse(String rawPayload) throws Exception {
    JsonNode root = objectMapper.readTree(rawPayload);

    if (!root.hasNonNull("timestamp")
        || !root.hasNonNull("current_state")
        || !root.hasNonNull("current_task")
        || !root.hasNonNull("current_task_duration")) {
      throw new IllegalArgumentException("Incomplete MQTT payload for workstation transport event mapping");
    }

    WorkstationTransportEventTransformationDto dto = new WorkstationTransportEventTransformationDto();
    dto.setTimestamp(root.get("timestamp").asText());
    dto.setCurrentState(root.get("current_state").asText());
    dto.setCurrentTask(root.get("current_task").asText());
    dto.setCurrentTaskDuration(root.get("current_task_duration").asDouble());
    return dto;
  }

  private Optional<WorkstationTransportEventDto> mapEventType(
      WorkstationTransportEventTransformationDto dto) {
    boolean isMoveCompletedNow = READY_STATE.equals(dto.getCurrentState()) && dto.getCurrentTask().isEmpty();
    boolean wasMoveCompletedBefore =
        lastSnapshot != null
            && READY_STATE.equals(lastSnapshot.getCurrentState())
            && lastSnapshot.getCurrentTask().isEmpty();

    if (isMoveCompletedNow && !wasMoveCompletedBefore) {
      return Optional.of(new WorkstationTransportEventDto(MOVE_COMPLETED_EVENT));
    }

    return Optional.empty();
  }
}
