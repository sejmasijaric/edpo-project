package org.unisg.ftengrave.factorysimulator.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MillingMachineMqttPayloadFactory {

  private final ObjectMapper objectMapper;
  private final MqttTimestampFactory timestampFactory;
  private final FactorySimulatorService factorySimulatorService;
  private final OneWayPointToPointTransportService millingMachineService;

  public MillingMachineMqttPayloadFactory(
      ObjectMapper objectMapper,
      MqttTimestampFactory timestampFactory,
      FactorySimulatorService factorySimulatorService,
      @Qualifier("millingMachineService") OneWayPointToPointTransportService millingMachineService) {
    this.objectMapper = objectMapper;
    this.timestampFactory = timestampFactory;
    this.factorySimulatorService = factorySimulatorService;
    this.millingMachineService = millingMachineService;
  }

  public String createPayload() {
    OneWayPointToPointTransportService.TransportMqttStatus status =
        millingMachineService.getMqttStatus();

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("timestamp", timestampFactory.createTimestamp());
    payload.put("i4_light_barrier", mapLightBarrier("MM-ejection"));
    payload.put("current_task", status.currentTask());
    payload.put("current_task_duration", status.currentTaskDurationSeconds());

    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize milling machine MQTT payload", exception);
    }
  }

  private int mapLightBarrier(String sinkId) {
    return factorySimulatorService.getSink(sinkId).item() == null ? 1 : 0;
  }
}
