package org.unisg.ftengrave.factorysimulator.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulationProperties;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.OvenService;

class OvenMqttPayloadFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
  private final OvenService ovenService = createOvenService();
  private final OvenMqttPayloadFactory payloadFactory =
      new OvenMqttPayloadFactory(
          objectMapper,
          new MqttTimestampFactory(),
          factorySimulatorService,
          ovenService);

  @Test
  void reportsOpenLightBarrierAndIdleTaskWhenOvenInputIsEmpty() throws Exception {
    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(1, payload.get("i5_light_barrier").asInt());
    assertEquals("", payload.get("current_task").asText());
    assertEquals(0.0d, payload.get("current_task_duration").asDouble());
  }

  @Test
  void reportsClosedLightBarrierWhenItemIsPresentAtOvenInput() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "VGR-oven");

    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(0, payload.get("i5_light_barrier").asInt());
  }

  private OvenService createOvenService() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setOvenBurnDuration(Duration.ZERO);
    return new OvenService(properties, "ov_1", 500, 100);
  }
}
