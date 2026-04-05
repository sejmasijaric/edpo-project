package org.unisg.ftengrave.factorysimulator.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class VacuumGripperMqttPayloadFactory {

  private final ObjectMapper objectMapper;
  private final MqttTimestampFactory timestampFactory;
  private final FactorySimulatorService factorySimulatorService;
  private final VacuumGripperService vacuumGripperService;

  public VacuumGripperMqttPayloadFactory(
      ObjectMapper objectMapper,
      MqttTimestampFactory timestampFactory,
      FactorySimulatorService factorySimulatorService,
      @Qualifier("vgrService") VacuumGripperService vacuumGripperService) {
    this.objectMapper = objectMapper;
    this.timestampFactory = timestampFactory;
    this.factorySimulatorService = factorySimulatorService;
    this.vacuumGripperService = vacuumGripperService;
  }

  public String createPayload() {
    VacuumGripperService.VacuumGripperMqttStatus status = vacuumGripperService.getMqttStatus();

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("timestamp", timestampFactory.createTimestamp());
    payload.put("i7_light_barrier", mapLightBarrier("SINK-I1"));
    payload.put("i4_light_barrier", mapLightBarrier("SINK-I2"));
    payload.put("current_task", status.currentTask());
    payload.put("current_task_duration", status.currentTaskDurationSeconds());

    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize vacuum gripper MQTT payload", exception);
    }
  }

  private int mapLightBarrier(String sinkId) {
    return factorySimulatorService.getSink(sinkId).item() == null ? 1 : 0;
  }
}
