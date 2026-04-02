package org.unisg.ftengrave.factorysimulator.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class WorkstationTransportMqttPayloadFactory {

  private final ObjectMapper objectMapper;
  private final MqttTimestampFactory timestampFactory;
  private final VacuumGripperService workstationTransportService;

  public WorkstationTransportMqttPayloadFactory(
      ObjectMapper objectMapper,
      MqttTimestampFactory timestampFactory,
      @Qualifier("wtService") VacuumGripperService workstationTransportService) {
    this.objectMapper = objectMapper;
    this.timestampFactory = timestampFactory;
    this.workstationTransportService = workstationTransportService;
  }

  public String createPayload() {
    VacuumGripperService.VacuumGripperMqttStatus status =
        workstationTransportService.getMqttStatus();

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("timestamp", timestampFactory.createTimestamp());
    payload.put("current_task", status.currentTask());
    payload.put("current_task_duration", status.currentTaskDurationSeconds());

    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException(
          "Failed to serialize workstation transport MQTT payload", exception);
    }
  }
}
