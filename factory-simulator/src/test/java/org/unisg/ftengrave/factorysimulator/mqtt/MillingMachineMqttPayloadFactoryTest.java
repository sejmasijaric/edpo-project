package org.unisg.ftengrave.factorysimulator.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulationProperties;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService;

class MillingMachineMqttPayloadFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
  private final OneWayPointToPointTransportService millingMachineService = createMillingMachineService();
  private final MillingMachineMqttPayloadFactory payloadFactory =
      new MillingMachineMqttPayloadFactory(
          objectMapper,
          new MqttTimestampFactory(),
          factorySimulatorService,
          millingMachineService);

  @Test
  void reportsOpenLightBarrierAndIdleTaskWhenEjectionIsEmpty() throws Exception {
    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(1, payload.get("i4_light_barrier").asInt());
    assertEquals("", payload.get("current_task").asText());
    assertEquals(0.0d, payload.get("current_task_duration").asDouble());
  }

  @Test
  void reportsClosedLightBarrierWhenItemIsPresentAtEjection() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "MM-ejection");

    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(0, payload.get("i4_light_barrier").asInt());
  }

  private OneWayPointToPointTransportService createMillingMachineService() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ZERO);
    return new OneWayPointToPointTransportService(
        factorySimulatorService,
        properties,
        "mm_1",
        "initial",
        "MM-initial",
        java.util.List.of(),
        "MM-ejection",
        0,
        0);
  }
}
